package liaison.groble.api.model.admin.settlement.response;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "관리자 페이지에서 정산 전체 정보에 대한 응답 DTO")
public class AdminSettlementsOverviewResponse {

  // 0. 정산 ID
  @Schema(
      description = "정산 ID",
      example = "1",
      type = "number",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private Long settlementId;

  // 1. 정산(예정)일
  @Schema(
      description = "정산 예정일 (년도 + 월 + 일)",
      example = "2023-02-10",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonFormat(pattern = "yyyy-MM-dd")
  private LocalDate scheduledSettlementDate;

  // 2. 콘텐츠 종류
  @Schema(
      description = "콘텐츠 유형 (COACHING: 서비스, DOCUMENT: 자료)",
      example = "DOCUMENT",
      type = "string",
      allowableValues = {"DOCUMENT", "COACHING"},
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String contentType;

  // 3. 정산(예정)금액
  @Schema(
      description = "정산(예정)금액 (표시 수수료 기준)",
      example = "1000000",
      type = "number",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private BigDecimal settlementAmount;

  // 4. 정산 상태
  @Schema(
      description =
          "정산 상태 (PENDING: 정산 예정, PROCESSING: 정산 처리중, COMPLETED: 정산 완료, ON_HOLD: 정산 보류, CANCELLED: 정산 취소, NOT_APPLICABLE: 정산 미해당)",
      example = "COMPLETED",
      allowableValues = {
        "PENDING",
        "PROCESSING",
        "COMPLETED",
        "ON_HOLD",
        "CANCELLED",
        "NOT_APPLICABLE"
      },
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String settlementStatus;

  @Schema(
      description = "판매자 닉네임",
      example = "메이커홍길동",
      type = "string",
      requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  private String nickname;

  @Schema(
      description = "결제 수단 [ONE_TIME - 일반 구매], [SUBSCRIPTION - 정기 구독]",
      example = "ONE_TIME",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String paymentType;

  // 5. 메이커 인증 여부
  @Schema(
      description = "메이커 인증 상태 (isSellerInfo가 true라는 전제 하에 사용됩니다.)",
      example = "VERIFIED",
      type = "string",
      allowableValues = {"PENDING", "IN_PROGRESS", "FAILED", "VERIFIED"},
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String verificationStatus;

  // 6. 판매자 정보
  @Schema(
      description = "개인 메이커 / 사업자 여부",
      example = "true",
      type = "boolean",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private Boolean isBusinessSeller;

  @Schema(
      description = "사업자 유형 (개인사업자-간이과세자, 개인사업자-일반과세자, 법인사업자)",
      example = "INDIVIDUAL_SIMPLE",
      type = "string",
      allowableValues = {"INDIVIDUAL_SIMPLE", "INDIVIDUAL_GENERAL", "CORPORATION"},
      requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  private String businessType;

  @Schema(
      description = "정산받을 예금주 이름 (계좌 소유자)",
      example = "홍길동",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String bankAccountOwner;

  @Schema(
      description = "정산받을 은행명",
      example = "국민은행",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String bankName;

  @Schema(
      description = "정산받을 계좌번호",
      example = "123-456-78901234",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String bankAccountNumber;

  @Schema(
      description = "통장 사본 파일 URL",
      example = "https://example.com/bankbook.jpg",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String copyOfBankbookUrl;

  @Schema(
      description = "사업자 등록증 파일 URL (isBusinessSeller가 true라는 전제 하에 사용됩니다.)",
      example = "https://example.com/business_license.jpg",
      type = "string",
      requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  private String businessLicenseFileUrl;

  @Schema(
      description = "세금계산서 수취 이메일",
      example = "example@example.com",
      type = "string",
      requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  private String taxInvoiceEmail;
}
