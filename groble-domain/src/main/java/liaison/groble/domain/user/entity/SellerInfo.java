package liaison.groble.domain.user.entity;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import liaison.groble.domain.user.enums.BusinessType;
import liaison.groble.domain.user.enums.SellerVerificationStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 판매자 정보를 관리하는 엔티티
 *
 * @author 권동민
 * @since 2025-07-27
 */
@Entity
@Table(name = "seller_infos")
@Getter
@Builder
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor
public class SellerInfo {
  @Id
  @GeneratedValue(strategy = IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", unique = true, nullable = false)
  private User user;

  /** 사업자 유형 (개인사업자-간이과세자, 개인사업자-일반과세자, 법인사업자) */
  @Column(name = "business_type")
  @Enumerated(EnumType.STRING)
  private BusinessType businessType;

  /** 판매자가 사업자 등록을 한 경우 true, 개인 판매자 등 사업자 등록이 없는 경우 false */
  @Column(name = "is_business_seller")
  private Boolean isBusinessSeller;

  /** 사업자 판매자 요청 여부 */
  @Column(name = "is_business_seller_request")
  private Boolean businessSellerRequest;

  /** 상호명 (사업체 이름) */
  @Column(name = "business_name")
  private String businessName;

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

  /** 기관 코드 */
  @Column(name = "bank_code", length = 10)
  private String bankCode;

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

  /** 메이커 인증 관련 피드백 메시지 (반려 사유 등) */
  @Column(name = "maker_verification_message", columnDefinition = "TEXT")
  private String makerVerificationMessage;

  /** 마지막 인증 시도 시간 */
  @Column(name = "last_verification_attempt")
  private LocalDateTime lastVerificationAttempt;

  /** 마지막 메이커 인증 시도 시간 */
  @Column(name = "maker_last_verification_attempt")
  private LocalDateTime makerLastVerificationAttempt;

  /** 통장 사본 첨부 URL */
  @Column(name = "copy_of_bankbook_url", columnDefinition = "TEXT")
  private String copyOfBankbookUrl;

  /** 생년월일 6자리 (YYMMDD) */
  @Column(name = "birth_date", length = 6)
  private String birthDate;

  /** 사업자등록번호 */
  @Column(name = "business_number")
  private String businessNumber;

  // 사업자 메이커 인증 요청 여부 확인
  public boolean isBusinessMakerVerificationRequested() {
    return businessLicenseFileUrl != null && !businessLicenseFileUrl.isBlank();
  }

  public void updateSellerVerificationStatus(SellerVerificationStatus status, String message) {
    this.verificationStatus = status;
    this.verificationMessage = message;
    this.lastVerificationAttempt = LocalDateTime.now();
  }

  public void updateSellerInfos(SellerInfo updatedInfo) {
    if (updatedInfo.getBusinessType() != null) {
      this.businessType = updatedInfo.getBusinessType();
    }
    if (updatedInfo.getIsBusinessSeller() != null) {
      this.isBusinessSeller = updatedInfo.getIsBusinessSeller();
    }
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
    if (updatedInfo.getRepresentativeName() != null) {
      this.representativeName = updatedInfo.getRepresentativeName();
    }
    if (updatedInfo.getBusinessLicenseFileUrl() != null) {
      this.businessLicenseFileUrl = updatedInfo.getBusinessLicenseFileUrl();
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
    if (updatedInfo.getCopyOfBankbookUrl() != null) {
      this.copyOfBankbookUrl = updatedInfo.getCopyOfBankbookUrl();
    }
    if (updatedInfo.getBirthDate() != null) {
      this.birthDate = updatedInfo.getBirthDate();
    }
    if (updatedInfo.getBusinessNumber() != null) {
      this.businessNumber = normalizeBusinessNumber(updatedInfo.getBusinessNumber());
    }
  }

  /**
   * 판매자 정보 유효성 검증 사업자 유형에 따라 필수 필드를 검증합니다.
   *
   * @return 필수 정보가 모두 입력되었으면 true, 아니면 false
   */
  public boolean isValidBusinessInfo() {
    // 공통 필수 정보 검증
    if (businessName == null
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

    // 세금계산서 이메일 형식 검증
    if (!taxInvoiceEmail.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$")) {
      return false;
    }

    return true;
  }

  // 판매자 정보 익명화 로직
  public void anonymize() {
    this.businessName = null;
    this.businessSector = null;
    this.businessCategory = null;
    this.businessAddress = null;
    this.representativeName = null;
    this.businessLicenseFileUrl = null;
    this.taxInvoiceEmail = null;
    this.bankName = null;
    this.bankAccountNumber = null;
    this.bankAccountOwner = null;
    this.birthDate = null;
    this.businessNumber = null;
    this.makerVerificationMessage = null;
    this.makerLastVerificationAttempt = null;
  }

  /**
   * 인증 상태 전용 팩토리
   *
   * @param status 초기 인증 상태
   */
  public static SellerInfo ofVerificationStatus(SellerVerificationStatus status) {
    SellerInfo info = new SellerInfo();
    info.verificationStatus = status;
    info.verificationMessage = null;
    info.makerVerificationMessage = null;
    info.makerLastVerificationAttempt = null;
    return info;
  }

  // 개인 메이커 은행 정보만 업데이트
  public void updatePersonalMakerBankInfo(
      String bankAccountOwner,
      String birthDate,
      String bankName,
      String bankAccountNumber,
      String copyOfBankbookUrl,
      String bankCode) {
    this.businessSellerRequest = false;
    this.bankAccountOwner = bankAccountOwner;
    this.birthDate = birthDate;
    this.bankName = bankName;
    this.bankAccountNumber = bankAccountNumber;
    this.copyOfBankbookUrl = copyOfBankbookUrl;
    this.bankCode = bankCode;
  }

  // 사업자 메이커 은행 정보만 업데이트
  public void updateBusinessMakerBankInfo(
      String bankAccountOwner,
      String birthDate,
      String bankName,
      String bankAccountNumber,
      String copyOfBankbookUrl,
      String bankCode) {
    this.businessSellerRequest = true;
    this.birthDate = birthDate;
    this.bankAccountOwner = bankAccountOwner;
    this.bankName = bankName;
    this.bankAccountNumber = bankAccountNumber;
    this.copyOfBankbookUrl = copyOfBankbookUrl;
    this.bankCode = bankCode;
  }

  // 사업자 정보만 업데이트
  public void updateBusinessInfo(
      BusinessType businessType,
      String businessNumber,
      String businessCategory,
      String businessSector,
      String businessName,
      String representativeName,
      String businessAddress,
      String businessLicenseFileUrl,
      String taxInvoiceEmail) {
    this.businessType = businessType;
    this.businessNumber = normalizeBusinessNumber(businessNumber);
    this.businessCategory = businessCategory;
    this.businessSector = businessSector;
    this.businessName = businessName;
    this.representativeName = representativeName;
    this.businessAddress = businessAddress;
    this.businessLicenseFileUrl = businessLicenseFileUrl;
    this.taxInvoiceEmail = taxInvoiceEmail;
  }

  // 메이커 인증 완료
  public void updateApprovedMaker(
      Boolean isBusinessSeller, SellerVerificationStatus sellerVerificationStatus) {
    this.isBusinessSeller = isBusinessSeller;
    this.verificationStatus = sellerVerificationStatus;
    this.makerVerificationMessage = null;
    this.makerLastVerificationAttempt = LocalDateTime.now();
  }

  // 메이커 인증 거절
  public void updateRejectedMaker(
      SellerVerificationStatus sellerVerificationStatus, String rejectionMessage) {
    this.verificationStatus = sellerVerificationStatus;
    this.makerVerificationMessage = rejectionMessage;
    this.makerLastVerificationAttempt = LocalDateTime.now();
  }

  // 판매자 정보 생성 팩토리 메소드
  public static SellerInfo createForUser(User user) {
    return SellerInfo.builder()
        .user(user)
        .verificationStatus(SellerVerificationStatus.PENDING)
        .build();
  }

  private String normalizeBusinessNumber(String businessNumber) {
    if (businessNumber == null) {
      return null;
    }

    String digitsOnly = businessNumber.replaceAll("\\D", "");
    if (digitsOnly.length() != 10) {
      return businessNumber;
    }

    return new StringBuilder(12)
        .append(digitsOnly, 0, 3)
        .append('-')
        .append(digitsOnly, 3, 5)
        .append('-')
        .append(digitsOnly, 5, 10)
        .toString();
  }
}
