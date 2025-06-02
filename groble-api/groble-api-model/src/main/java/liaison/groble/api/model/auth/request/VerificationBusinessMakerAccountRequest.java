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
  private String bankAccountOwner;

  /** 정산받을 은행명 */
  private String bankName;

  /** 정산받을 계좌번호 */
  private String bankAccountNumber;

  /** 통장 사본 첨부 URL */
  private String copyOfBankbookUrl;

  /** 사업자 유형 */
  private BusinessType businessType;

  /** 업종 */
  private String businessCategory;

  /** 업태 */
  private String businessSector;

  /** 상호 */
  private String businessName;

  /** 대표자 이름 (사업자등록증에 기재된 대표자명) */
  private String representativeName;

  /** 사업장 소재지 (사업자등록증에 기재된 주소) */
  private String businessAddress;

  /** 사업자등록증 첨부 URL */
  private String businessLicenseFileUrl;

  /** 세금계산서 수취 이메일 */
  private String taxInvoiceEmail;

  public enum BusinessType {
    INDIVIDUAL_SIMPLIFIED,
    INDIVIDUAL_NORMAL,
    CORPORATE
  }
}
