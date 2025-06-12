package liaison.groble.application.admin.service;

import org.springframework.stereotype.Service;

import liaison.groble.application.admin.dto.AdminOrderCancellationReasonDto;
import liaison.groble.application.order.service.OrderReader;
import liaison.groble.domain.order.entity.Order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminOrderService {
  private final OrderReader orderReader;

  public AdminOrderCancellationReasonDto getOrderCancellationReason(String merchantUid) {

    Order order = orderReader.getOrderByMerchantUid(merchantUid);
    if (!order.getStatus().equals(Order.OrderStatus.CANCELLED)) {
      throw new IllegalArgumentException("취소된 주문이 아닙니다.");
    }

    return AdminOrderCancellationReasonDto.builder().cancelReason(order.getOrderNote()).build();
  }
}
