package liaison.groble.api.model.payment.request;

import java.math.BigDecimal;
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

  // 주문명 (V2 API에서 필요)
  private String orderName;

  // 결제 금액 (컨트롤러에서 검증 가능)
  private BigDecimal amount;

  // 고객 정보
  private String customerName;
  private String customerEmail;
  private String customerPhone;

  // 결제 성공/실패 URL
  private String successUrl;
  private String failUrl;

  // PG 사업자
  private String pgProvider;

  // 카드 결제 옵션
  private CardOptions cardOptions;

  // 가상계좌 옵션
  private VirtualAccountOptions virtualAccountOptions;

  // 기타 추가 옵션
  private Map<String, Object> additionalOptions = new HashMap<>();

  @Getter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class CardOptions {
    // 할부 개월 수
    private Integer installment;
    // 카드사 직접 결제 여부
    private Boolean useCardPoint;
    // 해외카드 결제 여부
    private Boolean useInternationalCard;
  }

  @Getter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class VirtualAccountOptions {
    // 은행 코드
    private String bankCode;
    // 가상계좌 만료 시간(시간)
    private Integer validHours;
    // 현금영수증 발행 타입
    private String cashReceiptType;
  }
}
