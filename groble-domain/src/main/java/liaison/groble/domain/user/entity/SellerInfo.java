package liaison.groble.domain.user.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import liaison.groble.domain.user.enums.BusinessType;
import liaison.groble.domain.user.enums.SellerVerificationStatus;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SellerInfo {

  @Column(name = "business_name")
  private String businessName;

  @Column(name = "business_number")
  private String businessNumber; // 사업자등록번호

  @Column(name = "business_type")
  @Enumerated(EnumType.STRING)
  private BusinessType businessType;

  @Column(name = "business_category")
  private String businessCategory;

  @Column(name = "business_address")
  private String businessAddress;

  @Column(name = "representative_name")
  private String representativeName;

  @Column(name = "bank_name")
  private String bankName;

  @Column(name = "bank_account_number")
  private String bankAccountNumber;

  @Column(name = "bank_account_owner")
  private String bankAccountOwner;

  @Column(name = "verification_status")
  @Enumerated(EnumType.STRING)
  private SellerVerificationStatus verificationStatus = SellerVerificationStatus.PENDING;

  @Column(name = "verification_message")
  private String verificationMessage;

  @Column(name = "last_verification_attempt")
  private LocalDateTime lastVerificationAttempt;

  @Builder
  public SellerInfo(
      String businessName,
      String businessNumber,
      BusinessType businessType, // Enum으로 변경
      String businessCategory,
      String businessAddress,
      String representativeName,
      String bankName,
      String bankAccountNumber,
      String bankAccountOwner) {
    this.businessName = businessName;
    this.businessNumber = businessNumber;
    this.businessType = businessType;
    this.businessCategory = businessCategory;
    this.businessAddress = businessAddress;
    this.representativeName = representativeName;
    this.bankName = bankName;
    this.bankAccountNumber = bankAccountNumber;
    this.bankAccountOwner = bankAccountOwner;
    this.verificationStatus = SellerVerificationStatus.PENDING;
  }

  // 상태 변경 메서드
  public void updateVerificationStatus(SellerVerificationStatus status, String message) {
    this.verificationStatus = status;
    this.verificationMessage = message;
    this.lastVerificationAttempt = LocalDateTime.now();
  }
}
