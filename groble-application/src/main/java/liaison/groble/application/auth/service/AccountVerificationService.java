package liaison.groble.application.auth.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.auth.dto.VerifyBusinessMakerAccountDto;
import liaison.groble.application.auth.dto.VerifyPersonalMakerAccountDto;
import liaison.groble.application.user.service.UserReader;
import liaison.groble.domain.user.entity.User;
import liaison.groble.domain.user.enums.BusinessType;
import liaison.groble.domain.user.enums.SellerVerificationStatus;
import liaison.groble.domain.user.vo.SellerInfo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountVerificationService {

  private final UserReader userReader;

  @Transactional
  public void verifyPersonalMakerAccount(Long userId, VerifyPersonalMakerAccountDto dto) {
    User user = userReader.getUserById(userId);

    SellerInfo sellerInfo =
        SellerInfo.builder()
            .bankAccountOwner(dto.getBankAccountOwner())
            .bankName(dto.getBankName())
            .bankAccountNumber(dto.getBankAccountNumber())
            .build();

    user.getSellerInfo().update(sellerInfo);
    user.getSellerInfo().updateVerificationStatus(SellerVerificationStatus.IN_PROGRESS, null);
  }

  @Transactional
  public void verifyBusinessAccount(Long userId, VerifyBusinessMakerAccountDto dto) {
    User user = userReader.getUserById(userId);

    SellerInfo sellerInfo =
        SellerInfo.builder()
            .bankAccountOwner(dto.getBankAccountOwner())
            .bankName(dto.getBankName())
            .bankAccountNumber(dto.getBankAccountNumber())
            .copyOfBankbookUrl(dto.getCopyOfBankbookUrl())
            .businessType(BusinessType.valueOf(dto.getBusinessType().name()))
            .businessCategory(dto.getBusinessCategory())
            .businessSector(dto.getBusinessSector())
            .businessName(dto.getBusinessName())
            .representativeName(dto.getRepresentativeName())
            .businessAddress(dto.getBusinessAddress())
            .businessLicenseFileUrl(dto.getBusinessLicenseFileUrl())
            .taxInvoiceEmail(dto.getTaxInvoiceEmail())
            .build();

    user.getSellerInfo().update(sellerInfo);
    user.getSellerInfo().updateVerificationStatus(SellerVerificationStatus.IN_PROGRESS, null);
  }
}
