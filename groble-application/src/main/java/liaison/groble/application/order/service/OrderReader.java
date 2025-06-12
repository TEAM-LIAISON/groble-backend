package liaison.groble.application.order.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.common.exception.EntityNotFoundException;
import liaison.groble.domain.order.dto.FlatAdminOrderSummaryInfoDTO;
import liaison.groble.domain.order.entity.Order;
import liaison.groble.domain.order.repository.OrderCustomRepository;
import liaison.groble.domain.order.repository.OrderRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderReader {
  private final OrderRepository orderRepository;
  private final OrderCustomRepository orderCustomRepository;

  public Order getOrderByMerchantUid(String merchantUid) {
    return orderRepository
        .findByMerchantUid(merchantUid)
        .orElseThrow(
            () -> new EntityNotFoundException("주문을 찾을 수 없습니다. Merchant UID: " + merchantUid));
  }

  public Page<FlatAdminOrderSummaryInfoDTO> getAllOrders(Pageable pageable) {
    return orderCustomRepository.findOrdersByPageable(pageable);
  }
}
