package liaison.groble.application.admin.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.admin.dto.AdminMakerDetailInfoDTO;
import liaison.groble.application.notification.service.NotificationService;
import liaison.groble.application.user.service.UserReader;
import liaison.groble.domain.file.entity.FileInfo;
import liaison.groble.domain.file.repository.FileRepository;
import liaison.groble.domain.user.entity.User;
import liaison.groble.domain.user.enums.SellerVerificationStatus;
import liaison.groble.domain.user.vo.SellerInfo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminMakerService {
  // Repository
  private final UserReader userReader;
  private final FileRepository fileRepository;
  private final NotificationService notificationService;

  @Transactional(readOnly = true)
  public AdminMakerDetailInfoDTO getMakerDetailInfo(Long userId, String nickname) {
    User user = userReader.getUserByNickname(nickname);

    SellerInfo si = user.getSellerInfo();
    if (si == null) {
      return AdminMakerDetailInfoDTO.builder().build(); // 또는 throw new IllegalStateException(...)
    }

    String copyOfBankBookOriginalFileName = null;
    String businessLicenseOriginalFileName = null;

    if (si.getCopyOfBankbookUrl() != null) {
      FileInfo bankBookFileInfo = fileRepository.findByFileUrl(si.getCopyOfBankbookUrl());
      copyOfBankBookOriginalFileName = bankBookFileInfo.getOriginalFilename();
    }

    if (si.getBusinessLicenseFileUrl() != null) {
      FileInfo businessLicenseFileInfo =
          fileRepository.findByFileUrl(si.getBusinessLicenseFileUrl());
      businessLicenseOriginalFileName = businessLicenseFileInfo.getOriginalFilename();
    }

    /* 4) DTO 매핑 */
    return AdminMakerDetailInfoDTO.builder()
        .isBusinessMaker(si.getBusinessSellerRequest())
        .verificationStatus(si.getVerificationStatus().name())
        .bankAccountOwner(si.getBankAccountOwner())
        .bankName(si.getBankName())
        .bankAccountNumber(si.getBankAccountNumber())
        .copyOfBankBookOriginalFileName(copyOfBankBookOriginalFileName)
        .copyOfBankbookUrl(si.getCopyOfBankbookUrl())
        .businessType(si.getBusinessType() != null ? si.getBusinessType().name() : null)
        .businessCategory(si.getBusinessCategory())
        .businessSector(si.getBusinessSector())
        .businessName(si.getBusinessName())
        .representativeName(si.getRepresentativeName())
        .businessAddress(si.getBusinessAddress())
        .businessLicenseOriginalFileName(businessLicenseOriginalFileName)
        .businessLicenseFileUrl(si.getBusinessLicenseFileUrl())
        .taxInvoiceEmail(si.getTaxInvoiceEmail())
        .build();
  }

  @Transactional
  public void approveMaker(Long userId, String nickname) {
    User user = userReader.getUserByNickname(nickname);

    user.getSellerInfo()
        .updateApprovedMaker(
            user.isBusinessMakerVerificationRequested(), SellerVerificationStatus.VERIFIED);

    notificationService.sendMakerCertifiedVerificationNotification(user);

    log.info("사업자 메이커 인증 승인 처리: userId={}, nickname={}", userId, nickname);
  }

  @Transactional
  public void rejectMaker(Long userId, String nickname) {
    User user = userReader.getUserByNickname(nickname);

    user.getSellerInfo().updateRejectedMaker(SellerVerificationStatus.FAILED);

    notificationService.sendMakerRejectedVerificationNotification(user);

    log.info("사업자 메이커 인증 거절 처리: userId={}, nickname={}", userId, nickname);
  }
}
