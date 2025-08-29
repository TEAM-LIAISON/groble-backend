package liaison.groble.api.server.guest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.api.model.order.request.CreateOrderRequest;
import liaison.groble.application.order.dto.CreateOrderRequestDTO;
import liaison.groble.application.order.dto.CreateOrderSuccessDTO;
import liaison.groble.application.order.service.OrderService;
import liaison.groble.common.annotation.Auth;
import liaison.groble.common.annotation.GuestOnly;
import liaison.groble.common.annotation.Logging;
import liaison.groble.common.model.Accessor;
import liaison.groble.common.response.GrobleResponse;
import liaison.groble.mapping.order.OrderMapper;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/guest/order")
@Tag(name = "[👀 비회원] 비회원 주문/결제 기능", description = "비회원 주문 발행 기능")
public class GuestOrderController {
  // Service
  private final OrderService orderService;
  // Mapper
  private final OrderMapper orderMapper;

  @GuestOnly
  @PostMapping("/create")
  @Logging(
      item = "GuestOrder",
      action = "createGuestOrder",
      includeParam = true,
      includeResult = true)
  public ResponseEntity<GrobleResponse<Void>> createGuestOrder(
      @Auth Accessor accessor,
      @Valid @RequestBody CreateOrderRequest request,
      HttpServletRequest httpRequest) {
    CreateOrderRequestDTO createOrderRequestDTO = orderMapper.toCreateOrderDTO(request);
    CreateOrderSuccessDTO createOrderSuccessDTO =
        orderService.createOrderForGuest(createOrderRequestDTO, accessor.getUserId());

    return null;
  }
}
