package liaison.groble.application.order.strategy;

import java.util.List;

import org.springframework.stereotype.Component;

import liaison.groble.application.order.exception.OrderAuthenticationRequiredException;
import liaison.groble.common.context.UserContext;
import liaison.groble.common.strategy.ProcessorFactory;

@Component
public class OrderProcessorFactory {
  private final ProcessorFactory<OrderProcessorStrategy> processorFactory;

  public OrderProcessorFactory(List<OrderProcessorStrategy> processors) {
    this.processorFactory =
        new ProcessorFactory<>(
            processors, "주문", OrderAuthenticationRequiredException::forOrderCreation);
  }

  /**
   * 사용자 컨텍스트에 따른 적절한 주문 처리 전략을 반환
   *
   * @param userContext 사용자 컨텍스트
   * @return 주문 처리 전략
   */
  public OrderProcessorStrategy getProcessor(UserContext userContext) {
    return processorFactory.getProcessor(userContext);
  }
}
