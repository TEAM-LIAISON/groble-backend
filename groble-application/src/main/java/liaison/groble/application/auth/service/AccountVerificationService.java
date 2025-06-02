package liaison.groble.application.auth.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.auth.dto.VerifyBusinessMakerAccountDto;
import liaison.groble.application.auth.dto.VerifyPersonalMakerAccountDto;
import liaison.groble.application.user.service.UserReader;
import liaison.groble.domain.role.Role;
import liaison.groble.domain.role.repository.RoleRepository;
import liaison.groble.domain.user.entity.User;
import liaison.groble.domain.user.enums.BusinessType;
import liaison.groble.domain.user.enums.SellerVerificationStatus;
import liaison.groble.domain.user.vo.SellerInfo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountVerificationService {

  private final UserReader userReader;
  private final RoleRepository roleRepository;

  @Transactional
  public void verifyPersonalMakerAccount(Long userId, VerifyPersonalMakerAccountDto dto) {
    User user = userReader.getUserById(userId);

    SellerInfo sellerInfo =
        SellerInfo.builder()
            .isBusinessSeller(false)
            .bankAccountOwner(dto.getBankAccountOwner())
            .bankName(dto.getBankName())
            .bankAccountNumber(dto.getBankAccountNumber())
            .build();

    updateSellerVerification(user, sellerInfo);
  }

  @Transactional
  public void verifyBusinessAccount(Long userId, VerifyBusinessMakerAccountDto dto) {
    User user = userReader.getUserById(userId);

    SellerInfo sellerInfo =
        SellerInfo.builder()
            .isBusinessSeller(true)
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

    updateSellerVerification(user, sellerInfo);
  }

  private void updateSellerVerification(User user, SellerInfo sellerInfo) {
    user.getSellerInfo().update(sellerInfo);
    user.getSellerInfo().updateVerificationStatus(SellerVerificationStatus.IN_PROGRESS, null);

    Role roleSeller =
        roleRepository
            .findByName("ROLE_SELLER")
            .orElseThrow(() -> new RuntimeException("메이커 역할(ROLE_SELLER)을 찾을 수 없습니다."));

    // ✅ 이미 역할이 부여되어 있는지 확인
    boolean hasRole =
        user.getUserRoles().stream().anyMatch(userRole -> userRole.getRole().equals(roleSeller));

    if (!hasRole) {
      user.addRole(roleSeller);
    }
  }
}
