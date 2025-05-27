package liaison.groble.api.server.order;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.api.model.order.request.CreateOrderRequest;
import liaison.groble.api.model.order.response.OrderResponse;
import liaison.groble.api.server.order.mapper.OrderDtoMapper;
import liaison.groble.application.order.OrderService;
import liaison.groble.application.order.dto.OrderCreateDto;
import liaison.groble.common.annotation.Auth;
import liaison.groble.common.model.Accessor;
import liaison.groble.common.response.GrobleResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
@Tag(name = "Order", description = "주문 관련 API")
public class OrderController {

  private final OrderService orderService;
  private final OrderDtoMapper orderDtoMapper;

  @Operation(summary = "주문 생성", description = "콘텐츠 구매를 위한 주문을 생성합니다. 쿠폰 적용 가능")
  @PostMapping
  public ResponseEntity<GrobleResponse<OrderResponse>> createOrder(
      @Auth Accessor accessor, @RequestBody CreateOrderRequest request) {

    OrderCreateDto orderCreateDto =
        orderService.createOrder(
            accessor.getUserId(),
            request.getContentId(),
            request.getOptionType(),
            request.getOptionId(),
            request.getCouponCode());

    OrderResponse orderResponse = orderDtoMapper.toOrderResponse(orderCreateDto);

    return ResponseEntity.ok().body(GrobleResponse.success(orderResponse));
  }
}
