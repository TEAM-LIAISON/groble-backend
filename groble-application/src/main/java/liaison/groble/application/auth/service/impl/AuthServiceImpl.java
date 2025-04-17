package liaison.groble.application.auth.service.impl;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.auth.dto.EmailVerificationDto;
import liaison.groble.application.auth.dto.SignInDto;
import liaison.groble.application.auth.dto.SignUpDto;
import liaison.groble.application.auth.dto.TokenDto;
import liaison.groble.application.auth.dto.VerifyEmailCodeDto;
import liaison.groble.application.auth.exception.AuthenticationFailedException;
import liaison.groble.application.auth.exception.EmailAlreadyExistsException;
import liaison.groble.application.auth.service.AuthService;
import liaison.groble.common.port.security.SecurityPort;
import liaison.groble.common.utils.CodeGenerator;
import liaison.groble.domain.port.EmailSenderPort;
import liaison.groble.domain.port.VerificationCodePort;
import liaison.groble.domain.user.entity.IntegratedAccount;
import liaison.groble.domain.user.entity.Role;
import liaison.groble.domain.user.entity.SocialAccount;
import liaison.groble.domain.user.entity.User;
import liaison.groble.domain.user.entity.VerifiedEmail;
import liaison.groble.domain.user.enums.AccountType;
import liaison.groble.domain.user.enums.UserStatus;
import liaison.groble.domain.user.repository.IntegratedAccountRepository;
import liaison.groble.domain.user.repository.RoleRepository;
import liaison.groble.domain.user.repository.SocialAccountRepository;
import liaison.groble.domain.user.repository.UserRepository;
import liaison.groble.domain.user.repository.VerifiedEmailRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
  private final UserRepository userRepository;
  private final SecurityPort securityPort;
  private final EmailSenderPort emailSenderPort;
  private final VerificationCodePort verificationCodePort;
  private final RoleRepository roleRepository;
  private final IntegratedAccountRepository integratedAccountRepository;
  private final SocialAccountRepository socialAccountRepository;
  private final VerifiedEmailRepository verifiedEmailRepository;

  @Override
  @Transactional
  public TokenDto signUp(SignUpDto signUpDto) {
    // 통합 계정 이메일 중복 검사
    if (integratedAccountRepository.existsByIntegratedAccountEmail(signUpDto.getEmail())) {
      throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
    }

    VerifiedEmail verifiedEmail =
        verifiedEmailRepository
            .findByEmail(signUpDto.getEmail())
            .orElseThrow(() -> new IllegalArgumentException("인증 완료되지 않은 이메일입니다."));

    // 비밀번호 암호화
    String encodedPassword = securityPort.encodePassword(signUpDto.getPassword());

    // IntegratedAccount 생성 (내부적으로 User 객체 생성 및 연결)
    IntegratedAccount integratedAccount =
        IntegratedAccount.createAccount(verifiedEmail.getEmail(), encodedPassword);

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

    TokenDto tokenDto = issueTokens(savedUser);
    savedUser.updateRefreshToken(tokenDto.getRefreshToken());
    userRepository.save(savedUser);
    verifiedEmailRepository.deleteByEmail(verifiedEmail.getEmail());
    return tokenDto;
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

    return TokenDto.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .accessTokenExpiresIn(securityPort.getAccessTokenExpirationTime())
        .build();
  }

  @Override
  @Transactional
  public void logout(Long userId) {
    // 사용자 조회
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

    log.info("로그아웃 완료: {}", user.getEmail());
  }

  @Override
  @Transactional
  public void sendPasswordResetEmail(String email) {
    // 이메일로 사용자 계정 찾기
    IntegratedAccount account =
        integratedAccountRepository
            .findByIntegratedAccountEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("등록되지 않은 이메일입니다."));

    if (!account.getIntegratedAccountEmail().equals(email)) {
      throw new IllegalArgumentException("해당 회원의 이메일이 아닙니다.");
    }

    // 비밀번호 재설정 토큰 생성
    String token = UUID.randomUUID().toString();

    // 이메일 발송
    String resetLink = "https://dev.groble.im/reset-password?token=" + token;
    String emailContent =
        String.format(
            "안녕하세요,\n\n"
                + "비밀번호 재설정을 요청하셨습니다.\n"
                + "아래 링크를 클릭하여 새로운 비밀번호를 설정해주세요:\n\n"
                + "%s\n\n"
                + "이 링크는 24시간 동안 유효합니다.\n"
                + "비밀번호 재설정을 요청하지 않으셨다면 이 이메일을 무시하셔도 됩니다.\n\n"
                + "감사합니다.",
            resetLink);

    verificationCodePort.saveVerificationCode(email, token, 1440);
    emailSenderPort.sendPasswordResetEmail(email, emailContent);
  }

  @Override
  @Transactional
  public void resetPassword(Long userId, String token, String newPassword) {

    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

    IntegratedAccount account = user.getIntegratedAccount();

    verificationCodePort.validateVerificationCode(account.getIntegratedAccountEmail(), token);
    // 새 비밀번호 암호화
    String encodedPassword = securityPort.encodePassword(newPassword);

    // 비밀번호 업데이트
    account.updatePassword(encodedPassword);
    integratedAccountRepository.save(account);

    verificationCodePort.removeVerificationCode(account.getIntegratedAccountEmail());
  }

  @Override
  @Transactional
  public void sendEmailVerificationForSignUp(EmailVerificationDto emailVerificationDto) {
    String email = emailVerificationDto.getEmail();
    // 회원 생성용 이메일 인증

    // 이미 가입된 이메일인지 확인
    if (integratedAccountRepository.existsByIntegratedAccountEmail(email)) {
      throw new EmailAlreadyExistsException("이미 가입된 이메일입니다.");
    }

    // 인증 코드 생성 (4자리 숫자)
    String verificationCode = generateRandomCode();

    // 인증 코드 저장 (15분 유효) - 인터페이스 사용
    verificationCodePort.saveVerificationCode(email, verificationCode, 15);

    // 이메일 발송
    emailSenderPort.sendVerificationEmail(email, verificationCode);

    log.info("이메일 인증 코드 발송 완료: {}", email);
  }

  // 인증 코드 검증 메서드
  @Override
  @Transactional
  public void verifyEmailCode(VerifyEmailCodeDto verifyEmailCodeDto) {
    String email = verifyEmailCodeDto.getEmail();
    String code = verifyEmailCodeDto.getVerificationCode();

    // 인터페이스를 통한 인증 코드 검증
    boolean isValid = verificationCodePort.validateVerificationCode(email, code);

    if (!isValid) {
      throw new AuthenticationFailedException("인증 코드가 일치하지 않거나 만료되었습니다.");
    }

    if (!verifiedEmailRepository.existsByEmail(email)) {
      VerifiedEmail verifiedEmail = VerifiedEmail.createVerifiedEmail(email);
      verifiedEmailRepository.save(verifiedEmail);
    } else {
      throw new IllegalArgumentException("이미 인증된 이메일입니다.");
    }

    // 인증 코드 삭제
    verificationCodePort.removeVerificationCode(email);
  }

  @Override
  public void verifyEmailCodeForChangeEmail(Long userId, VerifyEmailCodeDto verifyEmailCodeDto) {
    String email = verifyEmailCodeDto.getEmail();
    String code = verifyEmailCodeDto.getVerificationCode();

    // 인터페이스를 통한 인증 코드 검증
    boolean isValid = verificationCodePort.validateVerificationCode(email, code);

    if (!isValid) {
      throw new AuthenticationFailedException("인증 코드가 일치하지 않거나 만료되었습니다.");
    }

    // 인증 코드 삭제
    verificationCodePort.removeVerificationCode(email);

    // 이메일 변경 요청과 새로운 회원 생성에 대한 인증 로직 처리
    if (userId != null) {
      User user =
          userRepository
              .findById(userId)
              .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

      AccountType accountType = user.getAccountType();
      if (accountType == AccountType.INTEGRATED) {
        // 통합 계정인 경우 이메일 변경 요청
        if (!integratedAccountRepository.existsByIntegratedAccountEmail(email)) {
          IntegratedAccount account = user.getIntegratedAccount();
          account.updateEmail(email);
          integratedAccountRepository.save(account);
        } else {
          throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
      } else {
        // 소셜 계정인 경우 이메일 변경 요청
        if (!socialAccountRepository.existsBySocialAccountEmail(email)) {
          SocialAccount account = user.getSocialAccount();
          account.updateEmail(email);
          socialAccountRepository.save(account);
        } else {
          throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
      }
    }
  }

  private String generateRandomCode() {
    return CodeGenerator.generateVerificationCode(4);
  }

  private TokenDto issueTokens(User user) {
    String accessToken = securityPort.createAccessToken(user.getId(), user.getEmail());
    String refreshToken = securityPort.createRefreshToken(user.getId(), user.getEmail());

    return TokenDto.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .accessTokenExpiresIn(securityPort.getAccessTokenExpirationTime())
        .build();
  }
}
