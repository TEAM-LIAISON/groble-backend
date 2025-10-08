package liaison.groble.application.admin.settlement.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PgFeeAdjustmentDTO {
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
