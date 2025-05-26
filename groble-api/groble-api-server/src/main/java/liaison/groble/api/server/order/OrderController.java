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

    log.info(
        "주문 생성 요청 - userId: {}, contentId: {}, couponCode: {}",
        accessor.getUserId(),
        request.getContentId(),
        request.getCouponCode());

    // 주문 생성 (쿠폰 적용 포함)
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
  //
  //
  //    @Operation(summary = "주문 상세 조회", description = "주문 ID로 주문 상세 정보를 조회합니다")
  //    @GetMapping("/{orderId}")
  //    public ResponseEntity<OrderDetailResponse> getOrder(
  //            @CurrentUser Long userId,
  //            @PathVariable String orderId) {
  //
  //        Order order = orderService.findByMerchantUid(orderId);
  //
  //        // 본인 주문인지 확인
  //        if (!order.getUser().getId().equals(userId)) {
  //            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
  //        }
  //
  //        OrderDetailResponse response = orderDtoMapper.toOrderDetailResponse(order);
  //        return ResponseEntity.ok(response);
  //    }
  //
  //    @Operation(summary = "내 주문 목록 조회", description = "로그인한 사용자의 주문 목록을 조회합니다")
  //    @GetMapping
  //    public ResponseEntity<List<OrderResponse>> getMyOrders(
  //            @CurrentUser Long userId,
  //            @RequestParam(defaultValue = "0") int page,
  //            @RequestParam(defaultValue = "20") int size) {
  //
  //        List<Order> orders = orderService.findByUserId(userId, page, size);
  //
  //        List<OrderResponse> responses = orders.stream()
  //            .map(orderDtoMapper::toOrderResponse)
  //            .collect(Collectors.toList());
  //
  //        return ResponseEntity.ok(responses);
  //    }
  //
  //    @Operation(summary = "주문 취소", description = "주문을 취소합니다. 결제 전 상태에서만 가능")
  //    @PostMapping("/{orderId}/cancel")
  //    public ResponseEntity<OrderResponse> cancelOrder(
  //            @CurrentUser Long userId,
  //            @PathVariable String orderId,
  //            @RequestParam(required = false) String reason) {
  //
  //        Order order = orderService.findByMerchantUid(orderId);
  //
  //        // 본인 주문인지 확인
  //        if (!order.getUser().getId().equals(userId)) {
  //            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
  //        }
  //
  //        // 주문 취소 (결제 전 상태에서만 가능)
  //        orderService.cancelOrder(order, reason != null ? reason : "사용자 요청");
  //
  //        OrderResponse response = orderDtoMapper.toOrderResponse(order);
  //        return ResponseEntity.ok(response);
  //    }
}
