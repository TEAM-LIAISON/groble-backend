package liaison.groble.application.payment.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.json.simple.JSONObject;

import liaison.groble.domain.payment.entity.PayplePayment;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentInfoDto {
  private String orderId;
  private Long userId;
  private BigDecimal amount;
  private String payMethod;
  private String status;
  private String productName;
  private LocalDateTime paymentDate;
  private String billingKey;
  private String cardName;
  private String cardNumber;
  private String receiptUrl;

  public static PaymentInfoDto from(PayplePayment payment, JSONObject apiResponse) {
    return PaymentInfoDto.builder()
        //        .orderId(payment.getOrderId())
        //        .userId(payment.getUserId())
        //        .amount(payment.getAmount())
        //        .payMethod(payment.getPayMethod())
        //        .status(payment.getStatus().name())
        //        .productName(payment.getProductName())
        //        .paymentDate(payment.getPaymentDate())
        //        .billingKey(payment.getBillingKey())
        //        .cardName((String) apiResponse.get("PCD_PAY_CARDNAME"))
        //        .cardNumber((String) apiResponse.get("PCD_PAY_CARDNUM"))
        //        .receiptUrl((String) apiResponse.get("PCD_PAY_CARDRECEIPT"))
        .build();
  }
}
