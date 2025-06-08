package liaison.groble.application.admin.service;

import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.notification.service.NotificationService;
import liaison.groble.application.user.service.UserReader;
import liaison.groble.domain.user.entity.User;
import liaison.groble.domain.user.enums.SellerVerificationStatus;
import liaison.groble.domain.user.vo.SellerInfo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminMakerService {
  private final UserReader userReader;
  private final NotificationService notificationService;

  @Transactional
  public void verifyMaker(Long userId, String nickname, String status) {
    final User user = userReader.getUserByNickname(nickname);

    if (Objects.equals(status, "APPROVED")) {
      // 수락 처리
      user.setSellerInfo(SellerInfo.ofVerificationStatus(SellerVerificationStatus.VERIFIED));
      notificationService.sendMakerCertifiedVerificationNotification(user);
    } else if (Objects.equals(status, "REJECTED")) {
      // 거절 처리
      user.setSellerInfo(SellerInfo.ofVerificationStatus(SellerVerificationStatus.FAILED));
      notificationService.sendMakerRejectedVerificationNotification(user);
    } else {
      throw new IllegalArgumentException("유효하지 않은 상태: " + status);
    }

    log.info("사업자 메이커 인증 요청 처리: userId={}, nickname={}, status={}", userId, nickname, status);
  }
}
