package liaison.groble.application.admin.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.admin.dto.AdminMakerDetailInfoDto;
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

  @Transactional(readOnly = true)
  public AdminMakerDetailInfoDto getMakerDetailInfo(Long userId, String nickname) {
    User user = userReader.getUserByNickname(nickname);

    SellerInfo si = user.getSellerInfo();
    if (si == null) {
      return AdminMakerDetailInfoDto.builder().build(); // 또는 throw new IllegalStateException(...)
    }

    /* 4) DTO 매핑 */
    return AdminMakerDetailInfoDto.builder()
        .isBusinessMaker(si.getIsBusinessSeller())
        .verificationStatus(si.getVerificationStatus().name())
        .bankAccountOwner(si.getBankAccountOwner())
        .bankName(si.getBankName())
        .bankAccountNumber(si.getBankAccountNumber())
        .copyOfBankbookUrl(si.getCopyOfBankbookUrl())
        .businessType(si.getBusinessType() != null ? si.getBusinessType().name() : null)
        .businessCategory(si.getBusinessCategory())
        .businessSector(si.getBusinessSector())
        .businessName(si.getBusinessName())
        .representativeName(si.getRepresentativeName())
        .businessAddress(si.getBusinessAddress())
        .businessLicenseFileUrl(si.getBusinessLicenseFileUrl())
        .taxInvoiceEmail(si.getTaxInvoiceEmail())
        .build();
  }

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
