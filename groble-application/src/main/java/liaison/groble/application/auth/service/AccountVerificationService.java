package liaison.groble.application.auth.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.auth.dto.VerifyBusinessMakerAccountDTO;
import liaison.groble.application.auth.dto.VerifyPersonalMakerAccountDTO;
import liaison.groble.application.auth.util.BankCodeUtil;
import liaison.groble.application.user.service.UserReader;
import liaison.groble.common.utils.BirthDateUtil;
import liaison.groble.domain.user.entity.SellerInfo;
import liaison.groble.domain.user.entity.User;
import liaison.groble.domain.user.enums.BusinessType;
import liaison.groble.domain.user.enums.SellerVerificationStatus;
import liaison.groble.external.discord.dto.BusinessMakerVerificationCreateReportDTO;
import liaison.groble.external.discord.dto.PersonalMakerVerificationCreateReportDTO;
import liaison.groble.external.discord.service.maker.DiscordBusinessMakerVerificationReportService;
import liaison.groble.external.discord.service.maker.DiscordPersonalMakerVerificationReportService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountVerificationService {

  // Repository
  private final UserReader userReader;

  // Discord
  private final DiscordPersonalMakerVerificationReportService
      discordPersonalMakerVerificationReportService;
  private final DiscordBusinessMakerVerificationReportService
      discordBusinessMakerVerificationReportService;

  @Transactional
  public void verifyPersonalMakerAccount(Long userId, VerifyPersonalMakerAccountDTO dto) {
    SellerInfo sellerInfo = userReader.getSellerInfoWithUser(userId);
    User user = sellerInfo.getUser();
    // 은행명으로 기관 코드 조회
    String bankCode = BankCodeUtil.getBankCode(dto.getBankName());
    String parsedBirthDate = BirthDateUtil.convertToSixDigit(dto.getBirthDate());

    // 직접 업데이트
    sellerInfo.updatePersonalMakerBankInfo(
        dto.getBankAccountOwner(),
        parsedBirthDate,
        dto.getBankName(),
        dto.getBankAccountNumber(),
        dto.getCopyOfBankbookUrl(),
        bankCode);

    final PersonalMakerVerificationCreateReportDTO personalMakerVerificationCreateReportDTO =
        PersonalMakerVerificationCreateReportDTO.builder()
            .userId(user.getId())
            .nickname(user.getNickname())
            .bankAccountOwner(dto.getBankAccountOwner())
            .bankName(dto.getBankName())
            .bankAccountNumber(dto.getBankAccountNumber())
            .copyOfBankbookUrl(dto.getCopyOfBankbookUrl())
            .build();

    discordPersonalMakerVerificationReportService.sendCreatePersonalMakerVerificationReport(
        personalMakerVerificationCreateReportDTO);
    updateSellerVerificationStatus(sellerInfo);
  }

  @Transactional
  public void verifyBusinessBankbook(Long userId, VerifyBusinessMakerAccountDTO dto) {
    SellerInfo sellerInfo = userReader.getSellerInfoWithUser(userId);

    // 은행명으로 기관 코드 조회
    String bankCode = BankCodeUtil.getBankCode(dto.getBankName());
    String parsedBirthDate = BirthDateUtil.convertToSixDigit(dto.getBirthDate());

    // 직접 업데이트
    sellerInfo.updateBusinessMakerBankInfo(
        dto.getBankAccountOwner(),
        parsedBirthDate,
        dto.getBankName(),
        dto.getBankAccountNumber(),
        dto.getCopyOfBankbookUrl(),
        bankCode);
  }

  @Transactional
  public void verifyBusinessAccount(Long userId, VerifyBusinessMakerAccountDTO dto) {
    SellerInfo sellerInfo = userReader.getSellerInfoWithUser(userId);
    User user = sellerInfo.getUser();
    // 직접 업데이트
    sellerInfo.updateBusinessInfo(
        BusinessType.valueOf(dto.getBusinessType().name()),
        dto.getBusinessCategory(),
        dto.getBusinessSector(),
        dto.getBusinessName(),
        dto.getRepresentativeName(),
        dto.getBusinessAddress(),
        dto.getBusinessLicenseFileUrl(),
        dto.getTaxInvoiceEmail());

    final BusinessMakerVerificationCreateReportDTO businessMakerVerificationCreateReportDTO =
        BusinessMakerVerificationCreateReportDTO.builder()
            .userId(user.getId())
            .nickname(user.getNickname())
            .bankAccountOwner(sellerInfo.getBankAccountOwner())
            .birthDate(sellerInfo.getBirthDate())
            .bankName(sellerInfo.getBankName())
            .bankAccountNumber(sellerInfo.getBankAccountNumber())
            .copyOfBankbookUrl(sellerInfo.getCopyOfBankbookUrl())
            .businessType(dto.getBusinessType().name())
            .businessCategory(dto.getBusinessCategory())
            .businessSector(dto.getBusinessSector())
            .businessName(dto.getBusinessName())
            .representativeName(dto.getRepresentativeName())
            .businessAddress(dto.getBusinessAddress())
            .businessLicenseFileUrl(dto.getBusinessLicenseFileUrl())
            .taxInvoiceEmail(dto.getTaxInvoiceEmail())
            .build();

    discordBusinessMakerVerificationReportService.sendCreateBusinessMakerVerificationReport(
        businessMakerVerificationCreateReportDTO);
    updateSellerVerificationStatus(sellerInfo);
  }

  private void updateSellerVerificationStatus(SellerInfo sellerInfo) {
    sellerInfo.updateSellerVerificationStatus(SellerVerificationStatus.IN_PROGRESS, null);
  }
}
