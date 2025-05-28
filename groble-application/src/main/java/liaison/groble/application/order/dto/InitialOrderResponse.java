package liaison.groble.application.order.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.Builder;
import lombok.Getter;

/**
 * 초기 주문 생성 응답 DTO
 *
 * <p>이 DTO는 서비스 레이어에서 컨트롤러로 주문 정보를 전달하는 역할을 합니다. 클라이언트가 결제 프로세스를 진행하는데 필요한 모든 정보를 포함합니다.
 */
@Getter
@Builder
public class InitialOrderResponse {

  // 주문 기본 정보
  private final Long orderId;
  private final String merchantUid; // 결제 시스템에서 사용할 고유 주문번호

  // 금액 정보 - 쿠폰 적용 전후 금액을 모두 제공
  private final BigDecimal originalAmount; // 원래 금액 (할인 전)
  private final BigDecimal discountAmount; // 할인 금액
  private final BigDecimal finalAmount; // 최종 결제 금액

  // 주문 항목 상세
  private final List<OrderItemResponse> orderItems;

  /**
   * 주문 항목 응답 DTO
   *
   * <p>각 옵션별 구매 정보를 담습니다. 클라이언트에서 주문 내역을 표시할 때 사용됩니다.
   */
  @Getter
  @Builder
  public static class OrderItemResponse {
    private final Long optionId;
    private final String optionType;
    private final Integer quantity;
    private final BigDecimal price; // 단가
    private final BigDecimal totalPrice; // 수량 × 단가
  }
}
