package liaison.groble.api.model.payment.request;

import java.util.HashMap;
import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentPrepareRequest {

  @NotNull(message = "주문 ID는 필수입니다")
  private Long orderId;

  @NotBlank(message = "결제 수단은 필수입니다")
  private String paymentMethod;

  private String customerName;
  private String customerEmail;
  private String customerPhone;
  private String successUrl;
  private String failUrl;

  /** 결제 요청에 필요한 추가 데이터 PG사 별로 필요한 파라미터를 담는 Map 객체 예: 가상계좌 은행 코드, 카드 할부 개월 수, 에스크로 여부 등 */
  private Map<String, Object> additionalData = new HashMap<>();
}
