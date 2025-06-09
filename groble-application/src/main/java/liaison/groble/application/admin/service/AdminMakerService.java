package liaison.groble.application.admin.service;

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
  public void approveMaker(Long userId, String nickname) {
    final User user = userReader.getUserByNickname(nickname);

    final SellerInfo sellerInfo =
        SellerInfo.builder()
            .isBusinessSeller(user.isBusinessMakerVerificationRequested())
            .verificationStatus(SellerVerificationStatus.VERIFIED)
            .build();

    user.setSellerInfo(sellerInfo);
    notificationService.sendMakerCertifiedVerificationNotification(user);

    log.info("사업자 메이커 인증 승인 처리: userId={}, nickname={}", userId, nickname);
  }

  @Transactional
  public void rejectMaker(Long userId, String nickname) {
    final User user = userReader.getUserByNickname(nickname);

    final SellerInfo sellerInfo =
        SellerInfo.builder().verificationStatus(SellerVerificationStatus.FAILED).build();

    user.setSellerInfo(sellerInfo);
    notificationService.sendMakerRejectedVerificationNotification(user);

    log.info("사업자 메이커 인증 거절 처리: userId={}, nickname={}", userId, nickname);
  }
}
