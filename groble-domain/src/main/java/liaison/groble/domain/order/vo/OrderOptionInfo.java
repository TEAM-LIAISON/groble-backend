package liaison.groble.domain.order.vo;

import java.math.BigDecimal;

import liaison.groble.domain.order.entity.OrderItem;

import lombok.Builder;
import lombok.Getter;

/**
 * 주문 옵션 정보 값 객체
 *
 * <p>Domain 계층에서 주문 생성 시 필요한 옵션 정보를 담는 값 객체입니다. 이를 통해 Domain이 Application의 DTO를 알 필요 없이 필요한 정보만
 * 전달받을 수 있습니다.
 *
 * <p>값 객체(Value Object)의 특징: - 불변성(Immutable): 한번 생성되면 상태가 변하지 않음 - 값으로 식별: ID가 아닌 속성 값들로 동일성 판단 -
 * 도메인 개념을 표현: 비즈니스 의미를 담은 객체
 */
@Getter
@Builder
public class OrderOptionInfo {
  private final Long optionId;
  private final OrderItem.OptionType optionType;
  private final BigDecimal price;
  private final Integer quantity;

  /**
   * 이 옵션의 총 금액을 계산합니다.
   *
   * @return 단가 × 수량
   */
  public BigDecimal getTotalPrice() {
    return price.multiply(BigDecimal.valueOf(quantity));
  }
}
