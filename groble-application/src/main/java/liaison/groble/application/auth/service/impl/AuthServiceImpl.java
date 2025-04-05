package liaison.groble.application.auth.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.auth.dto.SignInDto;
import liaison.groble.application.auth.dto.SignUpDto;
import liaison.groble.application.auth.dto.TokenDto;
import liaison.groble.application.auth.service.AuthService;
import liaison.groble.common.port.security.SecurityPort;
import liaison.groble.domain.user.entity.IntegratedAccount;
import liaison.groble.domain.user.entity.Role;
import liaison.groble.domain.user.entity.User;
import liaison.groble.domain.user.enums.UserStatus;
import liaison.groble.domain.user.repository.IntegratedAccountRepository;
import liaison.groble.domain.user.repository.RoleRepository;
import liaison.groble.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
  private final UserRepository userRepository;
  private final SecurityPort securityPort;
  private final RoleRepository roleRepository;
  private final IntegratedAccountRepository integratedAccountRepository;

  @Override
  @Transactional
  public TokenDto signUp(SignUpDto signUpDto) {
    // 통합 계정 이메일 중복 검사
    if (integratedAccountRepository.existsByIntegratedAccountEmail(signUpDto.getEmail())) {
      throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
    }

    // 비밀번호 암호화
    String encodedPassword = securityPort.encodePassword(signUpDto.getPassword());

    // IntegratedAccount 생성 (내부적으로 User 객체 생성 및 연결)
    IntegratedAccount integratedAccount =
        IntegratedAccount.createAccount(signUpDto.getEmail(), encodedPassword);

    User user = integratedAccount.getUser();

    // 기본 사용자 역할 추가
    Role userRole =
        roleRepository
            .findByName("ROLE_USER")
            .orElseThrow(() -> new RuntimeException("기본 역할(ROLE_USER)을 찾을 수 없습니다."));
    user.addRole(userRole);

    // 사용자 상태 활성화 설정
    user.updateStatus(UserStatus.ACTIVE);

    // 사용자 저장 (CascadeType.ALL로 IntegratedAccount도 함께 저장됨)
    User savedUser = userRepository.save(user);

    log.info("회원가입 완료: {}", savedUser.getEmail());

    // 토큰 생성
    String accessToken = securityPort.createAccessToken(savedUser.getId(), savedUser.getEmail());
    String refreshToken = securityPort.createRefreshToken(savedUser.getId(), savedUser.getEmail());

    // 리프레시 토큰 저장
    savedUser.updateRefreshToken(refreshToken);
    userRepository.save(savedUser);

    return TokenDto.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .accessTokenExpiresIn(securityPort.getAccessTokenExpirationTime())
        .build();
  }

  @Override
  @Transactional
  public TokenDto signIn(SignInDto signInDto) {
    // 이메일로 IntegratedAccount 찾기
    IntegratedAccount account =
        integratedAccountRepository
            .findByIntegratedAccountEmail(signInDto.getEmail())
            .orElseThrow(() -> new IllegalArgumentException("등록되지 않은 이메일입니다."));

    // 비밀번호 일치 여부 확인
    if (!securityPort.matches(signInDto.getPassword(), account.getPassword())) {
      throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
    }

    User user = account.getUser();
    // 사용자 상태 확인 (로그인 가능 상태인지)
    if (!user.isLoginable()) {
      throw new IllegalArgumentException("로그인할 수 없는 계정 상태입니다: " + user.getStatus());
    }

    // 로그인 시간 업데이트
    user.updateLoginTime();
    userRepository.save(user);

    log.info("로그인 성공: {}", user.getEmail());

    // 토큰 생성
    String accessToken = securityPort.createAccessToken(user.getId(), user.getEmail());
    String refreshToken = securityPort.createRefreshToken(user.getId(), user.getEmail());

    // 리프레시 토큰 저장
    user.updateRefreshToken(refreshToken);
    userRepository.save(user);

    return TokenDto.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .accessTokenExpiresIn(securityPort.getAccessTokenExpirationTime())
        .build();
  }

  @Override
  @Transactional(readOnly = true)
  public String getUserType(String email) {
    // 이메일로 계정 찾기
    IntegratedAccount account =
        integratedAccountRepository
            .findByIntegratedAccountEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("등록되지 않은 이메일입니다."));

    User user = account.getUser();

    // 기본 정보가 없는 경우
    if (user.getUserName() == null || user.getUserName().isEmpty()) {
      return "NONE";
    }

    // 사용자 역할 확인
    boolean isSeller =
        user.getUserRoles().stream()
            .anyMatch(userRole -> userRole.getRole().getName().equals("ROLE_SELLER"));
    boolean isBuyer =
        user.getUserRoles().stream()
            .anyMatch(userRole -> userRole.getRole().getName().equals("ROLE_USER"));

    // 둘 다 가진 경우 마지막 사용 역할 반환
    if (isSeller && isBuyer) {
      String lastUserType = user.getLastUserType();
      // 마지막 사용 역할이 없는 경우 기본값으로 BUYER 반환
      return lastUserType != null ? lastUserType : "BUYER";
    } else if (isSeller) {
      return "SELLER";
    } else {
      return "BUYER";
    }

    // // AuthenticationManager를 필드로 선언하되 생성자 주입은 하지 않음
    // private AuthenticationManager authenticationManager;
    //
    // // RequiredArgsConstructor 대신 생성자를 직접 정의하여 순환 참조 방지
    // public AuthService(
    // PasswordEncoder passwordEncoder,
    // UserRepository userRepository,
    // RoleRepository roleRepository,
    // IntegratedAccountRepository integratedAccountRepository,
    // SocialAccountRepository socialAccountRepository,
    // EmailVerificationService emailVerificationService,
    // JwtTokenProvider jwtTokenProvider) {
    // this.passwordEncoder = passwordEncoder;
    // this.userRepository = userRepository;
    // this.roleRepository = roleRepository;
    // this.integratedAccountRepository = integratedAccountRepository;
    // this.socialAccountRepository = socialAccountRepository;
    // this.emailVerificationService = emailVerificationService;
    // this.jwtTokenProvider = jwtTokenProvider;
    // }
    //
    // // 순환 참조 방지를 위해 AuthenticationManager는 setter 주입 사용
    // @Autowired
    // public void setAuthenticationManager(AuthenticationManager
    // authenticationManager) {
    // this.authenticationManager = authenticationManager;
    // }
    //
    // /**
    // * 회원가입 처리 이메일 인증 후 회원가입 처리
    // *
    // * @param request 회원가입 요청 정보
    // * @return 회원가입 결과 (토큰 포함)
    // * @throws EmailAlreadyExistsException 이미 등록된 이메일인 경우
    // * @throws EmailNotVerifiedException 이메일이 인증되지 않은 경우
    // */
    // @Transactional
    // public TokenResponse signup(SignupRequest request) {
    // String email = request.getEmail();
    //
    // // 이메일 중복 검사
    // if
    // (integratedAccountRepository.existsIntegratedAccountByIntegratedAccountEmail(email))
    // {
    // log.warn("이메일 중복: {}", email);
    // throw new EmailAlreadyExistsException(email);
    // }
    //
    // // 이메일 인증 여부 확인
    // if (!emailVerificationService.isEmailVerified(email)) {
    // log.warn("인증되지 않은 이메일로 회원가입 시도: {}", email);
    // throw new EmailNotVerifiedException(email);
    // }
    //
    // // 비밀번호 암호화
    // String encodedPassword = passwordEncoder.encode(request.getPassword());
    //
    // // 사용자 생성 - 인증된 이메일이므로 ACTIVE 상태로 생성
    // IntegratedAccount integratedAccount = IntegratedAccount.createAccount(email,
    // encodedPassword);
    // User savedUser = integratedAccount.getUser();
    // savedUser.updateStatus(UserStatus.ACTIVE);
    //
    // // 기본 역할 설정 (ROLE_USER)
    // Role userRole =
    // roleRepository
    // .findByName(RoleType.ROLE_USER.name())
    // .orElseThrow(() -> new RuntimeException("기본 역할(ROLE_USER)을 찾을 수
    // 없습니다."));
    // savedUser.addRole(userRole);
    //
    // // 사용자 저장
    // userRepository.save(savedUser);
    // log.info("회원가입 완료: {}", email);
    //
    // // 인증 정보 삭제 (재사용 방지)
    // emailVerificationService.expireVerification(email);
    //
    // // 토큰 발급
    // return generateTokenResponse(savedUser);
    // }
    //
    // /**
    // * 로그인 처리 통합 계정(이메일/비밀번호) 로그인 처리
    // *
    // * @param request 로그인 요청 정보
    // * @return 인증 토큰
    // * @throws AuthenticationFailedException 인증 실패 시
    // */
    // @Transactional
    // public TokenResponse login(LoginRequest request) {
    // try {
    // // Spring Security 인증 처리
    // Authentication authentication =
    // authenticationManager.authenticate(
    // new UsernamePasswordAuthenticationToken(request.getEmail(),
    // request.getPassword()));
    //
    // // UserDetailsImpl에서 사용자 ID 추출
    // Long userId = Long.parseLong(authentication.getName());
    //
    // // 사용자 조회
    // User user =
    // userRepository
    // .findById(userId)
    // .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
    //
    // // 사용자 상태 확인
    // if (!user.isLoginable()) {
    // log.warn("로그인 불가능한 상태의 계정: {}, 상태: {}", user.getEmail(), user.getStatus());
    // throw new AuthenticationFailedException(
    // "계정 상태가 로그인 불가능 상태입니다: " + user.getStatus().getStatusMessage());
    // }
    //
    // // 로그인 시간 업데이트
    // user.updateLoginTime();
    // userRepository.save(user);
    //
    // log.info("로그인 성공: {}", user.getEmail());
    //
    // // 토큰 발급
    // return generateTokenResponse(user);
    //
    // } catch (AuthenticationException e) {
    // log.warn("로그인 인증 실패: {}", request.getEmail());
    // throw new AuthenticationFailedException("이메일 또는 비밀번호가 일치하지 않습니다.");
    // }
    // }
    //
    // /**
    // * 소셜 로그인 처리 OAuth2 로그인 성공 후 처리 (토큰 발급)
    // *
    // * @param email 소셜 계정 이메일
    // * @param providerId 소셜 제공자 ID
    // * @param providerType 소셜 제공자 유형
    // * @return 인증 토큰
    // */
    // @Transactional
    // public TokenResponse socialLogin(String email, String providerId,
    // ProviderType providerType)
    // {
    // // 소셜 계정 조회
    // SocialAccount socialAccount =
    // socialAccountRepository
    // .findByProviderIdAndProviderType(providerId, providerType)
    // .orElse(null);
    //
    // User user;
    //
    // if (socialAccount == null) {
    // // 신규 소셜 로그인 - 새 계정 생성
    // user = createSocialUser(email, providerId, providerType);
    // } else {
    // // 기존 사용자 로그인
    // user = socialAccount.getUser();
    //
    // // 로그인 시간 업데이트
    // user.updateLoginTime();
    // userRepository.save(user);
    // }
    //
    // log.info("소셜 로그인 성공: {}, 제공자: {}", email, providerType);
    //
    // // 토큰 발급
    // return generateTokenResponse(user);
    // }
    //
    // /**
    // * 토큰 갱신 리프레시 토큰을 사용하여 액세스 토큰 갱신
    // *
    // * @param refreshToken 리프레시 토큰
    // * @return 새로운 토큰 세트
    // */
    // @Transactional
    // public TokenResponse refreshToken(String refreshToken) {
    // // 리프레시 토큰 유효성 검증
    // if (!jwtTokenProvider.validateToken(refreshToken)) {
    // throw new AuthenticationFailedException("유효하지 않은 리프레시 토큰입니다.");
    // }
    //
    // // 토큰에서 사용자 ID 추출
    // Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
    //
    // // 사용자 조회
    // User user =
    // userRepository
    // .findById(userId)
    // .orElseThrow(() -> new AuthenticationFailedException("사용자를 찾을 수
    // 없습니다."));
    //
    // // 저장된 리프레시 토큰과 일치하는지 확인
    // if (!refreshToken.equals(user.getRefreshToken())) {
    // throw new AuthenticationFailedException("저장된 리프레시 토큰과 일치하지 않습니다.");
    // }
    //
    // // 새 토큰 발급
    // return generateTokenResponse(user);
    // }
    //
    // /**
    // * 로그아웃 처리 리프레시 토큰 무효화
    // *
    // * @param userId 사용자 ID
    // */
    // @Transactional
    // public void logout(Long userId) {
    // User user =
    // userRepository.findById(userId).orElseThrow(() -> new RuntimeException("사용자를
    // 찾을
    // 수 없습니다."));
    //
    // // 리프레시 토큰 무효화
    // user.updateRefreshToken(null);
    // userRepository.save(user);
    //
    // log.info("로그아웃 처리 완료: {}", user.getEmail());
    // }
    //
    // /**
    // * 비밀번호 변경 현재 비밀번호 확인 후 새 비밀번호로 변경
    // *
    // * @param userId 사용자 ID
    // * @param currentPassword 현재 비밀번호
    // * @param newPassword 새 비밀번호
    // * @return 변경 성공 여부
    // */
    // @Transactional
    // public boolean changePassword(Long userId, String currentPassword, String
    // newPassword) {
    // User user =
    // userRepository.findById(userId).orElseThrow(() -> new RuntimeException("사용자를
    // 찾을
    // 수 없습니다."));
    //
    // // 통합 계정만 비밀번호 변경 가능
    // if (user.getAccountType() != AccountType.INTEGRATED ||
    // user.getIntegratedAccount() ==
    // null) {
    // log.warn("통합 계정이 아닌 사용자의 비밀번호 변경 시도: {}", user.getEmail());
    // throw new RuntimeException("통합 계정만 비밀번호 변경이 가능합니다.");
    // }
    //
    // // 현재 비밀번호 확인
    // if (!passwordEncoder.matches(currentPassword,
    // user.getIntegratedAccount().getPassword())) {
    // log.warn("현재 비밀번호 불일치로 비밀번호 변경 실패: {}", user.getEmail());
    // return false;
    // }
    //
    // // 새 비밀번호 암호화 및 저장
    // String encodedNewPassword = passwordEncoder.encode(newPassword);
    // user.getIntegratedAccount().updatePassword(encodedNewPassword);
    // userRepository.save(user);
    //
    // log.info("비밀번호 변경 완료: {}", user.getEmail());
    // return true;
    // }
    //
    // /**
    // * 비밀번호 재설정 이메일 인증 후 비밀번호 재설정 (비밀번호 분실 시 사용)
    // *
    // * @param email 사용자 이메일
    // * @param newPassword 새 비밀번호
    // * @return 변경 성공 여부
    // */
    // @Transactional
    // public boolean resetPassword(String email, String newPassword) {
    // // 이메일 인증 확인
    // if (!emailVerificationService.isEmailVerified(email)) {
    // log.warn("인증되지 않은 이메일로 비밀번호 재설정 시도: {}", email);
    // throw new EmailNotVerifiedException(email);
    // }
    //
    // // 통합 계정 조회
    // IntegratedAccount account =
    // integratedAccountRepository
    // .findByIntegratedAccountEmail(email)
    // .orElseThrow(() -> new RuntimeException("해당 이메일로 등록된 통합 계정이 없습니다."));
    //
    // // 새 비밀번호 암호화 및 저장
    // String encodedNewPassword = passwordEncoder.encode(newPassword);
    // account.updatePassword(encodedNewPassword);
    // integratedAccountRepository.save(account);
    //
    // // 인증 정보 삭제 (재사용 방지)
    // emailVerificationService.expireVerification(email);
    //
    // log.info("비밀번호 재설정 완료: {}", email);
    // return true;
    // }
    //
    // /** 계정 연동 (소셜 계정과 통합 계정 연결) 아직 구현되지 않음 */
    // @Transactional
    // public void linkAccount() {
    // // TODO: 계정 연동 기능 구현
    // }
    //
    // /**
    // * 신규 소셜 사용자 생성 소셜 로그인 정보로 새 사용자 생성
    // *
    // * @param email 이메일
    // * @param providerId 소셜 제공자 ID
    // * @param providerType 소셜 제공자 유형
    // * @return 생성된 사용자
    // */
    // private User createSocialUser(String email, String providerId, ProviderType
    // providerType) {
    // // 사용자 생성
    // SocialAccount socialAccount = SocialAccount.createAccount(providerId,
    // providerType,
    // email);
    //
    // // 저장된 사용자
    // User savedUser = socialAccount.getUser();
    //
    // // 기본 역할 설정 (ROLE_USER)
    // Role userRole =
    // roleRepository
    // .findByName(RoleType.ROLE_USER.name())
    // .orElseThrow(() -> new RuntimeException("기본 역할(ROLE_USER)을 찾을 수
    // 없습니다."));
    // savedUser.addRole(userRole);
    //
    // return userRepository.save(savedUser);
    // }
    //
    // /**
    // * 토큰 응답 생성 액세스 토큰과 리프레시 토큰 생성
    // *
    // * @param user 사용자
    // * @return 토큰 응답
    // */
    // private TokenResponse generateTokenResponse(User user) {
    // // 액세스 토큰 생성
    // String accessToken = jwtTokenProvider.createAccessToken(user);
    //
    // // 리프레시 토큰 생성
    // String refreshToken = jwtTokenProvider.createRefreshToken(user);
    //
    // // 리프레시 토큰 저장
    // user.updateRefreshToken(refreshToken);
    // userRepository.save(user);
    //
    // return TokenResponse.builder()
    // .accessToken(accessToken)
    // .refreshToken(refreshToken)
    // .tokenType("Bearer")
    // .expiresIn(jwtTokenProvider.getAccessTokenValidityInSeconds())
    // .build();
    // }
    //
    // /**
    // * 로그인 후 추가 정보 입력 처리 사용자 이름, ID, 동의 항목 등을 처리
    // *
    // * @param userId 사용자 ID
    // * @param request 추가 정보 요청
    // * @throws IllegalArgumentException 필수 동의 항목이 미동의인 경우
    // */
    // @Transactional
    // public void updateAdditionalInfo(Long userId, UserDTO.UserBasicInfoRequest
    // request) {
    // // 사용자 조회
    // User user =
    // userRepository.findById(userId).orElseThrow(() -> new RuntimeException("사용자를
    // 찾을
    // 수 없습니다."));
    //
    // // 사용자 이름 업데이트
    // user.updateUserName(request.getUserName());
    //
    // // 사용자 ID 업데이트
    // user.updateUserId(request.getUserId());
    //
    // // 마케팅 수신 동의 설정
    // user.setMarketingConsent(request.isMarketingConsent());
    //
    // // 변경사항 저장
    // userRepository.save(user);
    //
    // log.info("사용자 추가 정보 업데이트 완료: {}", user.getEmail());
    // }
  }
}
