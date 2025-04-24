package liaison.groble.application.payment.dto;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentPrepareDto {
  private Long orderId;
  private String paymentMethod;
  private String customerName;
  private String customerEmail;
  private String customerPhone;
  private String successUrl;
  private String failUrl;
  private Map<String, Object> additionalData;

  // 명세 기준
  private Long totalAmount;
  private Long taxFreeAmount;
}
