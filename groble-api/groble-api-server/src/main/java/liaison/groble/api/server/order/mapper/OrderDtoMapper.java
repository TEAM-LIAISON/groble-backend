package liaison.groble.api.server.order.mapper;

import org.springframework.stereotype.Component;

import liaison.groble.api.model.order.request.CreateOrderRequest;
import liaison.groble.api.model.order.response.OrderResponse;
import liaison.groble.application.order.dto.OrderCreateDto;

@Component
public class OrderDtoMapper {
  public OrderResponse toOrderResponse(OrderCreateDto orderCreateDto) {
    return OrderResponse.builder()
        .orderId(orderCreateDto.getOrderId())
        .contentId(orderCreateDto.getContentId())
        .optionId(orderCreateDto.getOptionId())
        .price(orderCreateDto.getPrice())
        .quantity(orderCreateDto.getQuantity())
        .totalPrice(orderCreateDto.getTotalPrice())
        .build();
  }

  public OrderCreateDto toServiceOrderCreateDto(CreateOrderRequest request) {
    return OrderCreateDto.builder()
        //        .contentId(request.getContentId())
        //        .contentOptionId(request.getContentOptionId())
        //        .price(request.getPrice())
        //        .quantity(request.getQuantity())
        //        .totalPrice(request.getTotalPrice())
        .build();
  }
}
