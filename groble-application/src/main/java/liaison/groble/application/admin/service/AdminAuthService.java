package liaison.groble.application.admin.service;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.auth.dto.SignInAuthResultDTO;
import liaison.groble.application.user.service.UserReader;
import liaison.groble.common.port.security.SecurityPort;
import liaison.groble.domain.user.entity.IntegratedAccount;
import liaison.groble.domain.user.entity.User;
import liaison.groble.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminAuthService {

  private final UserRepository userRepository;
  private final UserReader userReader;
  private final SecurityPort securityPort;

  @Transactional
  public SignInAuthResultDTO adminSignIn(String email, String password) {
    // 이메일로 IntegratedAccount 찾기
    IntegratedAccount integratedAccount = userReader.getUserByIntegratedAccountEmail(email);

    // 비밀번호 일치 여부 확인
    if (!securityPort.matches(password, integratedAccount.getPassword())) {
      throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
    }

    User user = integratedAccount.getUser();

    // 사용자 상태 확인 (로그인 가능 상태인지)
    if (!user.getUserStatusInfo().isLoginable()) {
      throw new IllegalArgumentException(
          "로그인할 수 없는 계정 상태입니다: " + user.getUserStatusInfo().getStatus());
    }

    // 로그인 시간 업데이트
    user.updateLoginTime();
    userRepository.save(user);

    log.info("로그인 성공: {}", user.getEmail());

    log.info("토큰 생성 시작: userId={}, email={}", user.getId(), user.getEmail());
    // 토큰 생성
    String accessToken = securityPort.createAccessToken(user.getId(), user.getEmail());
    String refreshToken = securityPort.createRefreshToken(user.getId(), user.getEmail());
    Instant refreshTokenExpiresAt = securityPort.getRefreshTokenExpirationTime(refreshToken);

    user.updateRefreshToken(refreshToken, refreshTokenExpiresAt);
    userRepository.save(user);

    log.info("리프레시 토큰 저장 완료: {}", user.getEmail());
    return SignInAuthResultDTO.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .hasAgreedToTerms(user.checkTermsAgreement())
        .hasNickname(user.hasNickname())
        .build();
  }
}
