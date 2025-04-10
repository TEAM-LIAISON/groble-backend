package liaison.groble.application.auth.service.impl;

import java.time.Duration;
import java.util.UUID;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.auth.dto.EmailVerificationDto;
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
import liaison.groble.security.service.TokenService;

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
  private final RedisTemplate<String, Object> redisTemplate;
  private final TokenService tokenService;

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

    // 리프레시 토큰을 Redis에 저장
    tokenService.saveRefreshToken(savedUser.getId().toString(), refreshToken);

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

    // 리프레시 토큰을 Redis에 저장
    tokenService.saveRefreshToken(user.getId().toString(), refreshToken);

    return TokenDto.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .accessTokenExpiresIn(securityPort.getAccessTokenExpirationTime())
        .build();
  }

  @Override
  @Transactional
  public void sendEmailVerification(EmailVerificationDto emailVerificationDto) {
    if (!integratedAccountRepository.existsByIntegratedAccountEmail(
        emailVerificationDto.getEmail())) {
      log.info("이메일 인증 메일 발송: {}", emailVerificationDto.getEmail());
    }
  }

  @Override
  @Transactional
  public void logout(Long userId) {
    // 사용자 조회
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

    // 리프레시 토큰을 Redis에서 삭제
    tokenService.deleteRefreshToken(userId.toString());

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

    // 비밀번호 재설정 토큰 생성
    String token = UUID.randomUUID().toString();

    // Redis에 토큰 저장 (24시간 유효)
    redisTemplate
        .opsForValue()
        .set("password_reset:" + token, account.getIntegratedAccountEmail(), Duration.ofHours(24));

    // 이메일 발송
    String resetLink = "https://groble.im/reset-password?token=" + token;
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

    // TODO: 이메일 발송 로직 구현
    log.info("비밀번호 재설정 이메일 발송: {}", email);
  }

  @Override
  @Transactional
  public void resetPassword(String email, String token, String newPassword) {
    // Redis에서 토큰 검증
    String storedEmail = (String) redisTemplate.opsForValue().get("password_reset:" + token);
    if (storedEmail == null || !storedEmail.equals(email)) {
      throw new IllegalArgumentException("유효하지 않거나 만료된 토큰입니다.");
    }

    // 사용자 계정 찾기
    IntegratedAccount account =
        integratedAccountRepository
            .findByIntegratedAccountEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("등록되지 않은 이메일입니다."));

    // 새 비밀번호 암호화
    String encodedPassword = securityPort.encodePassword(newPassword);

    // 비밀번호 업데이트
    account.updatePassword(encodedPassword);
    integratedAccountRepository.save(account);

    // 사용된 토큰 삭제
    redisTemplate.delete("password_reset:" + token);

    log.info("비밀번호 재설정 완료: {}", email);
  }
}
