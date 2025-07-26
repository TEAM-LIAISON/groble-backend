package liaison.groble.application.payment.dto;

import lombok.Builder;
import lombok.Getter;

/** 페이플 승인 결과 */
@Getter
@Builder
public class PaypleApprovalResult {
  private final boolean success;
  private final String payRst;
  private final String payCode;
  private final String payMsg;
  private final String payOid;
  private final String payType;
  private final String payTime;
  private final String payTotal;
  private final String payTaxTotal;
  private final String payIsTax;
  private final String payGoods;
  private final String payerName;
  private final String payerHp;
  private final String payCardName;
  private final String payCardNum;
  private final String payCardQuota;
  private final String payCardTradeNum;
  private final String payCardAuthNo;
  private final String payCardReceipt;
  private final String errorCode;
  private final String errorMessage;
}
