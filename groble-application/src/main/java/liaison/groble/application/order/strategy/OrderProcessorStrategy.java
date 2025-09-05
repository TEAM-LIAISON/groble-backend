package liaison.groble.application.order.strategy;

import jakarta.servlet.http.HttpServletRequest;

import liaison.groble.application.order.dto.CreateOrderRequestDTO;
import liaison.groble.application.order.dto.CreateOrderSuccessDTO;
import liaison.groble.application.order.dto.OrderSuccessDTO;
import liaison.groble.common.context.UserContext;
import liaison.groble.common.strategy.UserTypeProcessor;

public interface OrderProcessorStrategy extends UserTypeProcessor {
  CreateOrderSuccessDTO createOrder(
      UserContext userContext,
      CreateOrderRequestDTO createOrderRequestDTO,
      HttpServletRequest httpRequest);

  OrderSuccessDTO getOrderSuccess(UserContext userContext, String merchantUid);
}
