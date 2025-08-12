package liaison.groble.application.admin.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.admin.dto.AdminMakerDetailInfoDTO;
import liaison.groble.application.admin.dto.AdminMemoDTO;
import liaison.groble.application.notification.service.NotificationService;
import liaison.groble.application.user.service.UserReader;
import liaison.groble.application.user.service.UserWriter;
import liaison.groble.domain.file.entity.FileInfo;
import liaison.groble.domain.file.repository.FileRepository;
import liaison.groble.domain.market.entity.Market;
import liaison.groble.domain.user.entity.SellerInfo;
import liaison.groble.domain.user.entity.User;
import liaison.groble.domain.user.enums.SellerVerificationStatus;

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
  private final UserWriter userWriter;

  @Transactional(readOnly = true)
  public AdminMakerDetailInfoDTO getMakerDetailInfo(Long userId, String nickname) {
    SellerInfo sellerInfo = userReader.getSellerInfoWithUser(nickname);
    String marketLinkUrl = null;

    if (userReader.existsMarketByUserId(sellerInfo.getUser().getId())) {
      Market market = userReader.getMarket(sellerInfo.getUser().getId());
      marketLinkUrl = market.getMarketLinkUrl();
    }

    String copyOfBankBookOriginalFileName = null;
    String businessLicenseOriginalFileName = null;

    if (sellerInfo.getCopyOfBankbookUrl() != null) {
      FileInfo bankBookFileInfo = fileRepository.findByFileUrl(sellerInfo.getCopyOfBankbookUrl());
      copyOfBankBookOriginalFileName = bankBookFileInfo.getOriginalFilename();
    }

    if (sellerInfo.getBusinessLicenseFileUrl() != null) {
      FileInfo businessLicenseFileInfo =
          fileRepository.findByFileUrl(sellerInfo.getBusinessLicenseFileUrl());
      businessLicenseOriginalFileName = businessLicenseFileInfo.getOriginalFilename();
    }

    /* 4) DTO 매핑 */
    return AdminMakerDetailInfoDTO.builder()
        .isBusinessMaker(sellerInfo.getBusinessSellerRequest())
        .verificationStatus(sellerInfo.getVerificationStatus().name())
        .bankAccountOwner(sellerInfo.getBankAccountOwner())
        .bankName(sellerInfo.getBankName())
        .bankAccountNumber(sellerInfo.getBankAccountNumber())
        .copyOfBankBookOriginalFileName(copyOfBankBookOriginalFileName)
        .copyOfBankbookUrl(sellerInfo.getCopyOfBankbookUrl())
        .businessType(
            sellerInfo.getBusinessType() != null ? sellerInfo.getBusinessType().name() : null)
        .businessCategory(sellerInfo.getBusinessCategory())
        .businessSector(sellerInfo.getBusinessSector())
        .businessName(sellerInfo.getBusinessName())
        .representativeName(sellerInfo.getRepresentativeName())
        .businessAddress(sellerInfo.getBusinessAddress())
        .businessLicenseOriginalFileName(businessLicenseOriginalFileName)
        .businessLicenseFileUrl(sellerInfo.getBusinessLicenseFileUrl())
        .taxInvoiceEmail(sellerInfo.getTaxInvoiceEmail())
        .phoneNumber(sellerInfo.getUser().getPhoneNumber())
        .marketLinkUrl(marketLinkUrl)
        .adminMemo(sellerInfo.getUser().getAdminMemo())
        .build();
  }

  @Transactional
  public void approveMaker(String nickname) {
    SellerInfo sellerInfo = userReader.getSellerInfoWithUser(nickname);

    sellerInfo.updateApprovedMaker(
        sellerInfo.isBusinessMakerVerificationRequested(), SellerVerificationStatus.VERIFIED);

    notificationService.sendMakerCertifiedVerificationNotification(sellerInfo.getUser());
  }

  // 메이커 인증 반려 처리
  @Transactional
  public void rejectMaker(String nickname) {
    // 판매자 정보 조회 (User fetch join)
    SellerInfo sellerInfo = userReader.getSellerInfoWithUser(nickname);

    sellerInfo.updateRejectedMaker(SellerVerificationStatus.FAILED);

    notificationService.sendMakerRejectedVerificationNotificationAsync(
        sellerInfo.getUser().getId(), nickname);
  }

  @Transactional
  public AdminMemoDTO saveAdminMemo(Long userId, String nickname, AdminMemoDTO memoDTO) {
    User user = userReader.getUserByNickname(nickname);
    String adminMemo = memoDTO.getAdminMemo();
    user.setAdminMemo(adminMemo);

    return AdminMemoDTO.builder().adminMemo(adminMemo).build();
  }
}
