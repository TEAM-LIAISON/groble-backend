package liaison.groble.application.order;

import org.springframework.stereotype.Service;

import liaison.groble.application.order.dto.OrderCreateDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

  public void createOrder(Long userId, OrderCreateDto orderCreateDto) {}
}
