package liaison.groble.domain.user.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

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
  private String businessType;

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

  @Column(name = "approved")
  private boolean approved = false;

  @Column(name = "approved_at")
  private LocalDateTime approvedAt;

  @Builder
  public SellerInfo(
      String businessName,
      String businessNumber,
      String businessType,
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
  }

  public void approve() {
    this.approved = true;
    this.approvedAt = LocalDateTime.now();
  }
}
