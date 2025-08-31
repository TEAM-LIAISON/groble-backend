package liaison.groble.api.server.payment.processor;

import java.util.List;

import org.springframework.stereotype.Component;

import liaison.groble.application.payment.exception.PaymentAuthenticationRequiredException;
import liaison.groble.common.context.UserContext;
import liaison.groble.common.strategy.ProcessorFactory;

/**
 * 결제 처리 전략을 선택하는 팩토리 클래스
 *
 * <p>UserContext 따라 적절한 PaymentProcessor 반환
 *
 * <p>Strategy 패턴의 Context 역할을 수행, 확장 가능한 구조를 제공
 */
@Component
public class PaymentProcessorFactory {

  private final ProcessorFactory<PaymentProcessor> processorFactory;

  public PaymentProcessorFactory(List<PaymentProcessor> processors) {
    this.processorFactory =
        new ProcessorFactory<>(
            processors, "결제", PaymentAuthenticationRequiredException::forPayment);
  }

  /**
   * 사용자 컨텍스트에 따른 적절한 결제 처리 전략을 반환
   *
   * @param userContext 사용자 컨텍스트
   * @return 결제 처리 전략
   */
  public PaymentProcessor getProcessor(UserContext userContext) {
    return processorFactory.getProcessor(userContext);
  }
}
