package liaison.grobleauth.service;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import liaison.grobleauth.dto.AuthDto.TokenResponse;
import liaison.grobleauth.exception.OAuth2AuthenticationProcessingException;
import liaison.grobleauth.security.jwt.JwtTokenProvider;
import liaison.grobleauth.security.oauth2.OAuth2UserInfo;
import liaison.grobleauth.security.oauth2.OAuth2UserInfoFactory;
import liaison.groblecore.domain.ProviderType;
import liaison.groblecore.domain.Role;
import liaison.groblecore.domain.RoleType;
import liaison.groblecore.domain.User;
import liaison.groblecore.repository.RoleRepository;
import liaison.groblecore.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** OAuth2 인증 처리 서비스 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2AuthService extends DefaultOAuth2UserService {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final JwtTokenProvider jwtTokenProvider;
  private final RedisTemplate<String, Object> redisTemplate;

  // Redis에 토큰 저장 시 사용할 키 접두사
  private static final String REFRESH_TOKEN_KEY_PREFIX = "refresh_token:";

  /**
   * OAuth2 사용자 정보 로드 및 처리
   *
   * @param userRequest OAuth2 사용자 요청
   * @return OAuth2User 객체
   * @throws OAuth2AuthenticationException OAuth2 인증 예외
   */
  @Override
  @Transactional
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    try {
      OAuth2User oAuth2User = super.loadUser(userRequest);
      return processOAuth2User(userRequest, oAuth2User);
    } catch (AuthenticationException ex) {
      throw ex;
    } catch (Exception ex) {
      log.error("OAuth2 인증 처리 중 오류 발생", ex);
      throw new InternalAuthenticationServiceException(ex.getMessage(), ex);
    }
  }

  /**
   * OAuth2 사용자 정보 처리
   *
   * @param userRequest OAuth2 사용자 요청
   * @param oAuth2User OAuth2 사용자 정보
   * @return 처리된 OAuth2User 객체
   */
  @Transactional
  public OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
    // 소셜 로그인 제공자 ID (google, kakao, naver)
    String registrationId = userRequest.getClientRegistration().getRegistrationId();
    ProviderType providerType = getProviderType(registrationId);

    // OAuth2UserInfo 객체 생성
    OAuth2UserInfo userInfo =
        OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, oAuth2User.getAttributes());

    if (!StringUtils.hasText(userInfo.getEmail())) {
      throw new OAuth2AuthenticationProcessingException(
          "소셜 계정에서 이메일을 찾을 수 없습니다. " + providerType + " 계정에 연결된 이메일이 있는지 확인해주세요.");
    }

    // 기존 사용자 조회 (이메일 기준)
    Optional<User> userOptional = userRepository.findByEmail(userInfo.getEmail());
    User user;

    if (userOptional.isPresent()) {
      // 기존 사용자가 있는 경우
      user = userOptional.get();

      // 다른 소셜 로그인 제공자로 가입한 경우 처리
      if (!user.getProviderType().equals(providerType)) {
        log.warn(
            "이미 다른 제공자로 가입한 계정: {} (기존: {}, 신규: {})",
            userInfo.getEmail(),
            user.getProviderType(),
            providerType);

        throw new OAuth2AuthenticationProcessingException(
            "이미 " + user.getProviderType().name() + " 계정으로 가입하셨습니다. 해당 계정으로 로그인해주세요.");
      }

      // 사용자 정보 업데이트
      user.update(userInfo.getName(), userInfo.getAttributes().toString());
    } else {
      // 신규 사용자 등록
      user = registerNewUser(userInfo, providerType);
    }

    log.info("OAuth2 로그인 성공: {}, 제공자: {}", userInfo.getEmail(), providerType);

    return CustomOAuth2User.create(user, oAuth2User.getAttributes());
  }

  /**
   * 신규 사용자 등록
   *
   * @param userInfo OAuth2 사용자 정보
   * @param providerType 소셜 로그인 제공자 유형
   * @return 등록된 User 객체
   */
  @Transactional
  public User registerNewUser(OAuth2UserInfo userInfo, ProviderType providerType) {
    // 새 사용자 생성
    User user =
        User.createOAuth2User(
            userInfo.getEmail(),
            userInfo.getName(),
            null, // 프로필 이미지 URL
            providerType,
            userInfo.getId());

    // 기본 역할 설정 (ROLE_USER)
    Role userRole =
        roleRepository
            .findByName(RoleType.ROLE_USER)
            .orElseThrow(() -> new RuntimeException("기본 역할(ROLE_USER)을 찾을 수 없습니다."));
    user.addRole(userRole);

    log.info("OAuth2 신규 사용자 등록: {}, 제공자: {}", userInfo.getEmail(), providerType);
    return userRepository.save(user);
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

  /**
   * OAuth2 로그인 성공 후 토큰 생성
   *
   * @param email 사용자 이메일
   * @return 토큰 응답 객체
   */
  @Transactional
  public TokenResponse createTokens(String email) {
    // JWT 토큰 생성
    String accessToken = jwtTokenProvider.generateAccessToken(email);
    String refreshToken = jwtTokenProvider.generateRefreshToken(email);

    // 사용자 조회
    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

    // 리프레시 토큰 저장 (DB + Redis)
    user.updateRefreshToken(refreshToken);
    userRepository.save(user);

    // Redis에 리프레시 토큰 저장
    String redisKey = REFRESH_TOKEN_KEY_PREFIX + email;
    redisTemplate
        .opsForValue()
        .set(
            redisKey,
            refreshToken,
            jwtTokenProvider.getRefreshTokenExpirationMs(),
            TimeUnit.MILLISECONDS);

    // 토큰 응답 생성
    return TokenResponse.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .tokenType("Bearer")
        .expiresIn(jwtTokenProvider.getAccessTokenExpirationMs() / 1000)
        .build();
  }

  /** 커스텀 OAuth2User 구현 OAuth2User 인터페이스를 구현하고 사용자 정보를 관리 */
  public static class CustomOAuth2User implements OAuth2User {
    private final User user;
    private final Map<String, Object> attributes;
    private final Collection<? extends GrantedAuthority> authorities;

    private CustomOAuth2User(User user, Map<String, Object> attributes) {
      this.user = user;
      this.attributes = attributes;
      this.authorities = new HashSet<>(user.getRoles()); // Role은 이미 GrantedAuthority를 구현
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
      return authorities; // GrantedAuthority 호환 컬렉션 반환
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
