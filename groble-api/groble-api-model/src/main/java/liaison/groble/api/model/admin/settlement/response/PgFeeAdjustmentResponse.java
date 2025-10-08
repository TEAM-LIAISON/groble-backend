package liaison.groble.api.model.admin.settlement.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "[✅ 관리자 페이지 - 정산 관리] PG 수수료 차액 내역 응답")
public class PgFeeAdjustmentResponse {

  @Schema(description = "정산 ID", example = "123")
  private Long settlementId;

  @Schema(description = "정산 항목 ID", example = "456")
  private Long settlementItemId;

  @Schema(description = "구매 ID", example = "789")
  private Long purchaseId;

  @Schema(description = "판매자 ID", example = "42")
  private Long sellerId;

  @Schema(description = "판매자 닉네임", example = "groble_maker")
  private String sellerNickname;

  @Schema(description = "주문 고유번호 (Merchant UID)", example = "ORD-20250201-00001")
  private String merchantUid;

  @Schema(description = "콘텐츠 제목", example = "AI 툴 개발 가이드")
  private String contentTitle;

  @Schema(description = "총 판매 금액", example = "150000.00")
  private BigDecimal salesAmount;

  @Schema(description = "적용된 PG 수수료 (실제 2.9%)", example = "4350.00")
  private BigDecimal pgFeeApplied;

  @Schema(description = "표시된 PG 수수료 (기준 1.7%)", example = "2550.00")
  private BigDecimal pgFeeDisplay;

  @Schema(description = "PG 수수료 차액 (2.9% - 1.7%)", example = "1800.00")
  private BigDecimal pgFeeDifference;

  @Schema(description = "적용된 수수료 VAT", example = "435.00")
  private BigDecimal feeVat;

  @Schema(description = "표시된 수수료 VAT", example = "255.00")
  private BigDecimal feeVatDisplay;

  @Schema(description = "VAT 차액", example = "180.00")
  private BigDecimal feeVatDifference;

  @Schema(description = "환급 예상 PG 수수료(차액 + VAT)", example = "1980.00")
  private BigDecimal pgFeeRefundExpected;

  @Schema(description = "적용된 총 수수료", example = "4785.00")
  private BigDecimal totalFee;

  @Schema(description = "표시된 총 수수료", example = "2805.00")
  private BigDecimal totalFeeDisplay;

  @Schema(description = "적용된 정산 금액", example = "145215.00")
  private BigDecimal settlementAmount;

  @Schema(description = "표시된 정산 금액", example = "147195.00")
  private BigDecimal settlementAmountDisplay;

  @Schema(description = "구매 일시", example = "2025-02-01 12:34:56")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime purchasedAt;

  @Schema(description = "주문 상태 [PAID, CANCEL_REQUEST, CANCELLED]", example = "PAID")
  private String orderStatus;

  @Schema(description = "적용된 PG 수수료율", example = "0.0290")
  private BigDecimal capturedPgFeeRate;

  @Schema(description = "표시된 PG 수수료율", example = "0.0170")
  private BigDecimal capturedPgFeeRateDisplay;

  @Schema(description = "기준 PG 수수료율", example = "0.0170")
  private BigDecimal capturedPgFeeRateBaseline;

  @Schema(description = "적용된 VAT율", example = "0.1000")
  private BigDecimal capturedVatRate;
}
