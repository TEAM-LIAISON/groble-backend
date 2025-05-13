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

  /** 사업자 유형 (개인사업자-간이과세자, 개인사업자-일반과세자, 법인사업자) */
  @Column(name = "business_type")
  @Enumerated(EnumType.STRING)
  private BusinessType businessType;

  /** 상호명 (사업체 이름) */
  @Column(name = "business_name")
  private String businessName;

  /** 사업자등록번호 (000-00-00000 형식) */
  @Column(name = "business_number")
  private String businessNumber;

  /** 업태 (도소매업, 서비스업, 제조업 등) */
  @Column(name = "business_sector")
  private String businessSector;

  /** 업종 (의류, 전자제품, 식품 등 구체적인 사업 분야) */
  @Column(name = "business_category")
  private String businessCategory;

  /** 사업장 소재지 (사업자등록증에 기재된 주소) */
  @Column(name = "business_address")
  private String businessAddress;

  /** 대표자 이름 (사업자등록증에 기재된 대표자명) */
  @Column(name = "representative_name")
  private String representativeName;

  /** 사업자등록증 사본 파일 URL */
  @Column(name = "business_license_file_url", columnDefinition = "TEXT")
  private String businessLicenseFileUrl;

  /** 세금계산서 수취 이메일 */
  @Column(name = "tax_invoice_email")
  private String taxInvoiceEmail;

  /** 정산받을 은행명 */
  @Column(name = "bank_name")
  private String bankName;

  /** 정산받을 계좌번호 */
  @Column(name = "bank_account_number")
  private String bankAccountNumber;

  /** 예금주 이름 (계좌 소유자) */
  @Column(name = "bank_account_owner")
  private String bankAccountOwner;

  /** 판매자 인증 상태 (PENDING, IN_PROGRESS, FAILED, VERIFIED) */
  @Column(name = "verification_status")
  @Enumerated(EnumType.STRING)
  private SellerVerificationStatus verificationStatus = SellerVerificationStatus.PENDING;

  /** 인증 관련 피드백 메시지 (실패 이유 등) */
  @Column(name = "verification_message")
  private String verificationMessage;

  /** 마지막 인증 시도 시간 */
  @Column(name = "last_verification_attempt")
  private LocalDateTime lastVerificationAttempt;

  @Builder
  public SellerInfo(
      String businessName,
      String businessNumber,
      BusinessType businessType,
      String businessSector,
      String businessCategory,
      String businessAddress,
      String representativeName,
      String businessLicenseFileUrl,
      String taxInvoiceEmail,
      String bankName,
      String bankAccountNumber,
      String bankAccountOwner) {
    this.businessName = businessName;
    this.businessNumber = businessNumber;
    this.businessType = businessType;
    this.businessSector = businessSector;
    this.businessCategory = businessCategory;
    this.businessAddress = businessAddress;
    this.representativeName = representativeName;
    this.businessLicenseFileUrl = businessLicenseFileUrl;
    this.taxInvoiceEmail = taxInvoiceEmail;
    this.bankName = bankName;
    this.bankAccountNumber = bankAccountNumber;
    this.bankAccountOwner = bankAccountOwner;
    this.verificationStatus = SellerVerificationStatus.PENDING;
    this.lastVerificationAttempt = LocalDateTime.now();
  }

  /**
   * 판매자 인증 상태 업데이트
   *
   * @param status 변경할 인증 상태
   * @param message 상태 변경 관련 메시지 (인증 실패 이유 등)
   */
  public void updateVerificationStatus(SellerVerificationStatus status, String message) {
    this.verificationStatus = status;
    this.verificationMessage = message;
    this.lastVerificationAttempt = LocalDateTime.now();
  }

  /**
   * 판매자 정보 유효성 검증 사업자 유형에 따라 필수 필드를 검증합니다.
   *
   * @return 필수 정보가 모두 입력되었으면 true, 아니면 false
   */
  public boolean isValidBusinessInfo() {
    // 공통 필수 정보 검증
    if (businessName == null
        || businessNumber == null
        || businessType == null
        || businessAddress == null
        || representativeName == null
        || businessLicenseFileUrl == null
        || taxInvoiceEmail == null
        || bankName == null
        || bankAccountNumber == null
        || bankAccountOwner == null) {
      return false;
    }

    // 사업자등록번호 형식 검증 (000-00-00000)
    if (!businessNumber.matches("\\d{3}-\\d{2}-\\d{5}")) {
      return false;
    }

    // 세금계산서 이메일 형식 검증
    if (!taxInvoiceEmail.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$")) {
      return false;
    }

    return true;
  }

  /**
   * 판매자 정보 업데이트
   *
   * @param updatedInfo 업데이트할 정보가 담긴 SellerInfo 객체
   */
  public void update(SellerInfo updatedInfo) {
    if (updatedInfo.getBusinessName() != null) {
      this.businessName = updatedInfo.getBusinessName();
    }
    if (updatedInfo.getBusinessSector() != null) {
      this.businessSector = updatedInfo.getBusinessSector();
    }
    if (updatedInfo.getBusinessCategory() != null) {
      this.businessCategory = updatedInfo.getBusinessCategory();
    }
    if (updatedInfo.getBusinessAddress() != null) {
      this.businessAddress = updatedInfo.getBusinessAddress();
    }
    if (updatedInfo.getTaxInvoiceEmail() != null) {
      this.taxInvoiceEmail = updatedInfo.getTaxInvoiceEmail();
    }
    if (updatedInfo.getBankName() != null) {
      this.bankName = updatedInfo.getBankName();
    }
    if (updatedInfo.getBankAccountNumber() != null) {
      this.bankAccountNumber = updatedInfo.getBankAccountNumber();
    }
    if (updatedInfo.getBankAccountOwner() != null) {
      this.bankAccountOwner = updatedInfo.getBankAccountOwner();
    }
  }

  public void anonymize() {
    this.businessName = "탈퇴한 판매자";
  }
}
