package liaison.groble.api.model.auth.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "개인 • 법인 사업자 인증 처리 요청")
public class VerificationBusinessMakerAccountRequest {
  /** 정산받을 은행 계좌 소유자 이름 */
  @Schema(description = "정산받을 은행 계좌 소유자 이름", example = "홍길동")
  private String bankAccountOwner;

  /** 정산받을 은행명 */
  @Schema(description = "정산받을 은행명", example = "신한은행")
  private String bankName;

  /** 정산받을 계좌번호 */
  @Schema(description = "정산받을 은행명", example = "신한은행")
  private String bankAccountNumber;

  /** 통장 사본 첨부 URL */
  @Schema(
      description = "통장 사본 첨부 URL",
      example = "https://image.dev.groble.im/bankbook/honggildong.jpg")
  private String copyOfBankbookUrl;

  /** 사업자 유형 */
  @Schema(
      description =
          "사업자 유형 (INDIVIDUAL_SIMPLIFIED: 개인사업자 간이과세자, INDIVIDUAL_NORMAL: 개인사업자 일반과세자, CORPORATE: 법인사업자)",
      example = "INDIVIDUAL_NORMAL",
      allowableValues = {"INDIVIDUAL_SIMPLIFIED", "INDIVIDUAL_NORMAL", "CORPORATE"})
  private BusinessType businessType;

  /** 업종 */
  @Schema(description = "업종", example = "IT 서비스")
  private String businessCategory;

  /** 업태 */
  @Schema(description = "업태", example = "소프트웨어 개발 및 공급업")
  private String businessSector;

  /** 상호 */
  @Schema(description = "상호", example = "그로블컴퍼니")
  private String businessName;

  /** 대표자 이름 (사업자등록증에 기재된 대표자명) */
  @Schema(description = "대표자 이름 (사업자등록증에 기재된 대표자명)", example = "홍길동")
  private String representativeName;

  /** 사업장 소재지 (사업자등록증에 기재된 주소) */
  @Schema(description = "사업장 소재지 (사업자등록증에 기재된 주소)", example = "서울특별시 강남구 테헤란로 123")
  private String businessAddress;

  /** 사업자등록증 첨부 URL */
  @Schema(
      description = "사업자등록증 첨부 URL",
      example = "https://image.dev.groble.im/license/1234567890.jpg")
  private String businessLicenseFileUrl;

  /** 세금계산서 수취 이메일 */
  @Schema(description = "세금계산서 수취 이메일", example = "hong@company.com")
  private String taxInvoiceEmail;

  @Schema(description = "사업자 유형 ENUM")
  public enum BusinessType {
    INDIVIDUAL_SIMPLIFIED,
    INDIVIDUAL_NORMAL,
    CORPORATE
  }
}
