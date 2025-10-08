package liaison.groble.domain.settlement.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 정산 항목별 PG 수수료 차액(실제 2.9% - 기준 1.7%)을 조회하기 위한 평탄화 DTO. */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlatPgFeeAdjustmentDTO {
  private Long settlementId;
  private Long settlementItemId;
  private Long purchaseId;
  private Long sellerId;
  private String sellerNickname;
  private String merchantUid;
  private String contentTitle;
  private BigDecimal salesAmount;
  private BigDecimal pgFeeApplied;
  private BigDecimal pgFeeDisplay;
  private BigDecimal pgFeeDifference;
  private BigDecimal feeVat;
  private BigDecimal feeVatDisplay;
  private BigDecimal feeVatDifference;
  private BigDecimal pgFeeRefundExpected;
  private BigDecimal totalFee;
  private BigDecimal totalFeeDisplay;
  private BigDecimal settlementAmount;
  private BigDecimal settlementAmountDisplay;
  private LocalDateTime purchasedAt;
  private String orderStatus;
  private BigDecimal capturedPgFeeRate;
  private BigDecimal capturedPgFeeRateDisplay;
  private BigDecimal capturedPgFeeRateBaseline;
  private BigDecimal capturedVatRate;
}
