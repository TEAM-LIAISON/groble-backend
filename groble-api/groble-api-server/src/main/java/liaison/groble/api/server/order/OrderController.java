package liaison.groble.api.server.order;

import jakarta.validation.Valid;

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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "주문 관련 API")
public class OrderController {

  private final OrderService orderService;
  private final OrderDtoMapper orderDtoMapper;

  @Deprecated
  @PostMapping
  @Operation(summary = "주문 생성", description = "콘텐츠 정보를 받아 주문을 생성하고 주문 ID를 반환합니다.")
  @ApiResponse(
      responseCode = "201",
      description = "주문 생성 성공",
      content = @Content(schema = @Schema(implementation = OrderResponse.class)))
  public void createOrder(@Auth Accessor accessor, @Valid @RequestBody CreateOrderRequest request) {
    OrderCreateDto orderCreateDto = orderDtoMapper.toServiceOrderCreateDto(request);
    orderService.createOrder(accessor.getUserId(), orderCreateDto);
  }
}
