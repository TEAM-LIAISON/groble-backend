package liaison.groble.application.admin.settlement.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 페이플 이체 가능 잔액 조회 DTO. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaypleAccountRemainDTO {

  private String result;
  private String message;
  private String code;
  private BigDecimal totalAccountAmount;
  private BigDecimal totalTransferAmount;
  private BigDecimal remainAmount;
  private String apiTranDtm;
  private BigDecimal cumulativePgFeeRefundExpected;

  public boolean isSuccess() {
    return "A0000".equalsIgnoreCase(result);
  }
}
