package liaison.groble.application.order.dto;

import java.math.BigDecimal;

import liaison.groble.domain.order.entity.OrderItem;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ValidatedOrderOptionDTO {
  private final Long optionId;
  private final OrderItem.OptionType optionType;
  private final BigDecimal price;
  private final Integer quantity;
}
