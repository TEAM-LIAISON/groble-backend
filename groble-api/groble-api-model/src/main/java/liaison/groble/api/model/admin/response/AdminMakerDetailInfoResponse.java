package liaison.groble.api.model.admin.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "관리자 페이지에서 메이커 상세 정보에 대한 응답 DTO")
public class AdminMakerDetailInfoResponse {
  @Schema(
      description = "개인 메이커인지 사업자 메이커인지 여부",
      example = "true",
      type = "boolean",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private Boolean isBusinessMaker;

  @Schema(
      description = "메이커 인증 상태 (isSellerInfo가 true라는 전제 하에 사용됩니다.)",
      example = "VERIFIED",
      type = "string",
      allowableValues = {"PENDING", "IN_PROGRESS", "FAILED", "VERIFIED"},
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String verificationStatus;

  /** 1. 정산 예금주 */
  @Schema(
      description = "정산 계좌 예금주 이름",
      example = "홍길동",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String bankAccountOwner;

  /** 2. 정산 은행명 */
  @Schema(
      description = "정산용 은행명",
      example = "카카오뱅크",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String bankName;

  /** 3. 계좌번호 */
  @Schema(
      description = "정산 계좌번호",
      example = "3333-02-1234567",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String bankAccountNumber;

  /** 4. 통장 사본 URL */
  @Schema(
      description = "통장 사본 이미지 URL",
      example = "https://cdn.example.com/bankbook/abc123.jpg",
      type = "string",
      format = "uri",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String copyOfBankbookUrl;

  /** 5. 사업자 유형 */
  @Schema(
      description = "사업자 유형 (예: 개인·간이, 개인·일반, 법인)",
      example = "individual-simple",
      type = "string",
      allowableValues = {"individual-simple", "individual-general", "corporation"},
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String businessType;

  /** 6. 업종 */
  @Schema(
      description = "사업자 업종(업태)",
      example = "IT 서비스업",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String businessCategory;

  /** 7. 업태 */
  @Schema(
      description = "업태(예: 제조, 서비스, 도소매 등)",
      example = "서비스",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String businessSector;

  /** 8. 상호(사업체명) */
  @Schema(
      description = "사업자 등록 상의 상호명",
      example = "링킷",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String businessName;

  /** 9. 대표자 이름 */
  @Schema(
      description = "대표자명",
      example = "김철수",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String representativeName;

  /** 10. 사업장 주소 */
  @Schema(
      description = "사업장 소재지 주소",
      example = "서울특별시 강남구 테헤란로 123 4F",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String businessAddress;

  /** 11. 사업자등록증 사본 URL */
  @Schema(
      description = "사업자등록증 파일 URL",
      example = "https://cdn.example.com/license/xyz789.pdf",
      type = "string",
      format = "uri",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String businessLicenseFileUrl;

  /** 12. 세금계산서 수취 이메일 */
  @Schema(
      description = "세금계산서 수령 이메일",
      example = "tax@example.com",
      type = "string",
      format = "email",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String taxInvoiceEmail;
}
