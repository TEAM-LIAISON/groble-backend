package liaison.groble.security.service;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import liaison.groble.domain.role.Role;
import liaison.groble.domain.role.enums.RoleType;
import liaison.groble.domain.role.repository.RoleRepository;
import liaison.groble.domain.user.entity.SocialAccount;
import liaison.groble.domain.user.entity.User;
import liaison.groble.domain.user.enums.ProviderType;
import liaison.groble.domain.user.enums.UserStatus;
import liaison.groble.domain.user.factory.UserFactory;
import liaison.groble.domain.user.repository.SocialAccountRepository;
import liaison.groble.domain.user.repository.UserRepository;
import liaison.groble.security.oauth2.exception.OAuth2AuthenticationProcessingException;
import liaison.groble.security.oauth2.userinfo.OAuth2UserInfo;
import liaison.groble.security.oauth2.userinfo.OAuth2UserInfoFactory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** OAuth2 인증 서비스 소셜 로그인(Google, Kakao, Naver) 처리 및 관리 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2AuthService extends DefaultOAuth2UserService {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final SocialAccountRepository socialAccountRepository;

  /**
   * OAuth2 사용자 정보 로드 및 처리 Spring Security OAuth2의 UserService 인터페이스 구현
   *
   * @param userRequest OAuth2 사용자 요청 객체
   * @return OAuth2User 객체
   * @throws OAuth2AuthenticationException OAuth2 인증 예외
   */
  @Override
  @Transactional
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    try {
      // 상위 클래스의 loadUser 메서드 호출하기 전에 로깅 추가
      log.info(
          "OAuth2 요청 정보: clientId={}, registrationId={}",
          userRequest.getClientRegistration().getClientId(),
          userRequest.getClientRegistration().getRegistrationId());

      OAuth2User oAuth2User = super.loadUser(userRequest);
      log.info("OAuth2 사용자 속성 (after loadUser): {}", oAuth2User.getAttributes());

      // 속성 로깅 추가
      log.info("OAuth2 사용자 속성: {}", oAuth2User.getAttributes());

      return processOAuth2User(userRequest, oAuth2User);
    } catch (Exception ex) {
      log.error("OAuth2 인증 처리 중 오류 발생", ex);
      throw new InternalAuthenticationServiceException(ex.getMessage(), ex);
    }
  }

  /**
   * 신규 사용자 등록 소셜 로그인 정보로 새로운 사용자 생성
   *
   * @param userInfo OAuth2 사용자 정보
   * @param providerType 소셜 로그인 제공자 유형
   * @return 등록된 User 객체
   */
  @Transactional
  public User registerNewUser(OAuth2UserInfo userInfo, ProviderType providerType) {
    // Use the UserFactory to create a social user
    // This is better than direct SocialAccount.createAccount which might create coupling issues
    User user =
        UserFactory.createSocialUser(
            providerType.name(), // Provider name (GOOGLE, KAKAO, NAVER)
            userInfo.getId(), // Provider-specific ID
            userInfo.getEmail(), // Email from the provider
            null // Name from the provider (or null if not available)
            );

    // Set profile image if available from social provider
    if (StringUtils.hasText(userInfo.getImageUrl())) {
      user.getUserProfile().updateProfileImageUrl(userInfo.getImageUrl());
    }

    // Add default role (ROLE_USER)
    Role userRole =
        roleRepository
            .findByName(RoleType.ROLE_USER.toString())
            .orElseThrow(() -> new RuntimeException("기본 역할(ROLE_USER)을 찾을 수 없습니다."));
    user.addRole(userRole);

    // Social login accounts are immediately activated
    // Note: UserFactory might have already set this, but we ensure it here
    user.getUserStatusInfo().updateStatus(UserStatus.ACTIVE);

    // Save the user to the database
    return userRepository.save(user);
  }

  /**
   * OAuth2 사용자 정보 처리 소셜 로그인 정보를 DB와 대조하여 신규 가입 또는 로그인 처리
   *
   * @param userRequest OAuth2 사용자 요청 객체
   * @param oAuth2User OAuth2 사용자 정보 객체
   * @return 처리된 OAuth2User 객체
   */
  @Transactional
  public OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
    // 소셜 로그인 제공자 ID (google, kakao, naver)
    String registrationId = userRequest.getClientRegistration().getRegistrationId();
    ProviderType providerType = getProviderType(registrationId);

    String userNameAttributeName =
        userRequest
            .getClientRegistration()
            .getProviderDetails()
            .getUserInfoEndpoint()
            .getUserNameAttributeName(); // ex: "sub", "id", "response.id"

    Map<String, Object> attributes = oAuth2User.getAttributes();

    if (!attributes.containsKey(userNameAttributeName)) {
      throw new IllegalArgumentException(
          "Missing attribute '" + userNameAttributeName + "' in attributes");
    }

    // OAuth2UserInfo 객체 생성 (제공자별 데이터 구조 차이 처리)
    OAuth2UserInfo userInfo =
        OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, oAuth2User.getAttributes());

    // 이메일 정보 유효성 검증
    if (!StringUtils.hasText(userInfo.getEmail())) {
      log.warn("소셜 계정에서 이메일 정보를 찾을 수 없음: {}, 제공자: {}", userInfo.getId(), providerType);
      throw new OAuth2AuthenticationProcessingException(
          "소셜 계정에서 이메일을 찾을 수 없습니다. " + providerType + " 계정에 연결된 이메일이 있는지 확인해주세요.");
    }

    log.debug(
        "OAuth2 사용자 정보: 이메일={}, 제공자ID={}, 제공자={}",
        userInfo.getEmail(),
        userInfo.getId(),
        providerType);

    // 기존 사용자 조회 (소셜 계정 기준)
    Optional<SocialAccount> socialAccountOptional =
        socialAccountRepository.findByProviderIdAndProviderType(userInfo.getId(), providerType);

    User user;

    if (socialAccountOptional.isPresent()) {
      // 기존 소셜 계정으로 로그인하는 경우
      user = socialAccountOptional.get().getUser();
      log.info("기존 소셜 계정으로 로그인: {}, 제공자: {}", userInfo.getEmail(), providerType);

      // 소셜 계정 정보 업데이트 필요 시 처리 (예: 프로필 이미지 변경)
      updateExistingSocialUser(user, userInfo);
    } else {
      // 신규 사용자 등록 (새 소셜 계정)
      user = registerNewUser(userInfo, providerType);
      log.info("신규 소셜 사용자 등록 완료: {}, 제공자: {}", userInfo.getEmail(), providerType);
    }

    // 로그인 시간 업데이트
    user.updateLoginTime();
    userRepository.save(user);

    // ⚠ 필수 키가 없는 경우 추가해주는 안전 장치
    if (!attributes.containsKey("sub") && "google".equals(registrationId)) {
      attributes.put("sub", userInfo.getId());
    }
    if (!attributes.containsKey("id") && "kakao".equals(registrationId)) {
      attributes.put("id", userInfo.getId());
    }

    // 커스텀 OAuth2User 객체 생성 및 반환
    return CustomOAuth2User.create(user, oAuth2User.getAttributes());
  }

  /**
   * 기존 소셜 사용자 정보 업데이트 프로필 이미지 등 변경 사항 반영
   *
   * @param user 기존 사용자 객체
   * @param userInfo 신규 OAuth2 사용자 정보
   */
  private void updateExistingSocialUser(User user, OAuth2UserInfo userInfo) {
    boolean isChanged = false;

    // 변경 사항이 있는 경우만 저장
    if (isChanged) {
      userRepository.save(user);
      log.debug("기존 소셜 사용자 정보 업데이트: {}", userInfo.getEmail());
    }
  }

  /**
   * 소셜 로그인 제공자 ID를 ProviderType으로 변환
   *
   * @param registrationId 소셜 로그인 제공자 ID
   * @return ProviderType
   */
  private ProviderType getProviderType(String registrationId) {
    return switch (registrationId.toLowerCase()) {
      case "google" -> ProviderType.GOOGLE;
      case "kakao" -> ProviderType.KAKAO;
      case "naver" -> ProviderType.NAVER;
      default -> throw new OAuth2AuthenticationProcessingException(
          "지원하지 않는 소셜 로그인 제공자입니다: " + registrationId);
    };
  }

  /** 커스텀 OAuth2User 클래스 Spring Security OAuth2User 인터페이스 구현 */
  public static class CustomOAuth2User implements OAuth2User {
    private final User user;
    private final Map<String, Object> attributes;
    private final Collection<? extends GrantedAuthority> authorities;

    private CustomOAuth2User(User user, Map<String, Object> attributes) {
      this.user = user;
      this.attributes = attributes;
      this.authorities =
          user.getUserRoles().stream()
              .map(userRole -> new SimpleGrantedAuthority(userRole.getRole().getName()))
              .collect(Collectors.toSet());
    }

    public static CustomOAuth2User create(User user, Map<String, Object> attributes) {
      return new CustomOAuth2User(user, attributes);
    }

    @Override
    public Map<String, Object> getAttributes() {
      return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
      return authorities;
    }

    @Override
    public String getName() {
      return user.getEmail();
    }

    public Long getId() {
      return user.getId();
    }

    public String getEmail() {
      return user.getEmail();
    }
  }
}
