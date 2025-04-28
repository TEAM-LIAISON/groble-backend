package liaison.groble.application.order;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.user.service.UserReader;
import liaison.groble.common.exception.EntityNotFoundException;
import liaison.groble.domain.content.repository.ContentRepository;
import liaison.groble.domain.order.entity.Order;
import liaison.groble.domain.order.repository.OrderRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
  private final UserReader userReader;
  private final ContentRepository contentRepository;
  private final OrderRepository orderRepository;

  @Transactional
  public void completePayment(Long orderId) {
    Order order =
        orderRepository
            .findById(orderId)
            .orElseThrow(() -> new EntityNotFoundException("주문을 찾을 수 없습니다."));

    order.completePayment();
    orderRepository.save(order);

    // 간단한 이벤트 발행
    //        eventPublisher.publishEvent(new OrderPaidEvent(order));
  }
}
