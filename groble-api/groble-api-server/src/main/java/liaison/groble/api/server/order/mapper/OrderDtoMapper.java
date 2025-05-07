package liaison.groble.api.server.order.mapper;

import org.springframework.stereotype.Component;

import liaison.groble.api.model.order.request.CreateOrderRequest;
import liaison.groble.application.order.dto.OrderCreateDto;

@Component
public class OrderDtoMapper {
  public OrderCreateDto toServiceOrderCreateDto(CreateOrderRequest request) {
    return OrderCreateDto.builder()
        .contentId(request.getContentId())
        .contentOptionId(request.getContentOptionId())
        .price(request.getPrice())
        .quantity(request.getQuantity())
        .totalPrice(request.getTotalPrice())
        .build();
  }
}
