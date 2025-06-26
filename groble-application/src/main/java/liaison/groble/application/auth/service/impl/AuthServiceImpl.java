package liaison.groble.application.auth.service.impl;

import java.time.Instant;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.auth.dto.SignInAuthResultDTO;
import liaison.groble.application.auth.dto.SignInDTO;
import liaison.groble.application.auth.dto.UserWithdrawalDTO;
import liaison.groble.application.auth.service.AuthService;
import liaison.groble.application.content.ContentReader;
import liaison.groble.application.user.service.UserReader;
import liaison.groble.common.port.security.SecurityPort;
import liaison.groble.domain.user.entity.IntegratedAccount;
import liaison.groble.domain.user.entity.User;
import liaison.groble.domain.user.entity.UserWithdrawalHistory;
import liaison.groble.domain.user.enums.WithdrawalReason;
import liaison.groble.domain.user.repository.UserRepository;
import liaison.groble.domain.user.repository.UserWithdrawalHistoryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
  private final UserReader userReader;
  private final ContentReader contentReader;
  private final UserRepository userRepository;
  private final SecurityPort securityPort;
  private final UserWithdrawalHistoryRepository userWithdrawalHistoryRepository;

  @Override
  @Transactional
  public SignInAuthResultDTO signIn(SignInDTO signInDto) {
    // 이메일로 IntegratedAccount 찾기
    IntegratedAccount integratedAccount =
        userReader.getUserByIntegratedAccountEmail(signInDto.getEmail());

    // 비밀번호 일치 여부 확인
    if (!securityPort.matches(signInDto.getPassword(), integratedAccount.getPassword())) {
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

  @Override
  @Transactional
  public void withdrawUser(Long userId, UserWithdrawalDTO dto) {
    // 1. 사용자 조회 및 검증
    User user = userReader.getUserById(userId);

    validateWithdrawalEligibility(user);
    // 2. 탈퇴 사유 처리
    WithdrawalReason reason = parseWithdrawalReason(dto.getReason());

    // 3. 회원 탈퇴 처리 (User 엔티티에 캡슐화)
    user.withdraw();
    user.anonymize();

    // 4. 탈퇴 이력 저장
    saveWithdrawalHistory(user, reason, dto.getAdditionalComment());
  }

  private void validateWithdrawalEligibility(User user) {
    // 판매 중인 콘텐츠가 있으면 탈퇴 불가
    boolean hasSellingContents = contentReader.existsSellingContentByUser(user.getId());
    if (hasSellingContents) {
      throw new IllegalStateException("판매 중인 콘텐츠가 있어 탈퇴할 수 없습니다.");
    }

    if (user.isWithdrawn()) {
      throw new IllegalArgumentException("이미 탈퇴한 사용자입니다.");
    }
  }

  private WithdrawalReason parseWithdrawalReason(String reasonStr) {
    if (reasonStr == null || reasonStr.trim().isEmpty()) {
      return WithdrawalReason.OTHER; // 기본값
    }

    try {
      return WithdrawalReason.valueOf(reasonStr.toUpperCase());
    } catch (IllegalArgumentException e) {
      log.warn("Invalid withdrawal reason: {}", reasonStr);
      return WithdrawalReason.OTHER;
    }
  }

  private void saveWithdrawalHistory(User user, WithdrawalReason reason, String comment) {
    userWithdrawalHistoryRepository.save(
        UserWithdrawalHistory.builder()
            .userId(user.getId())
            .email(user.getEmail())
            .reason(reason)
            .additionalComment(comment)
            .withdrawalDate(LocalDateTime.now())
            .build());
  }
}
