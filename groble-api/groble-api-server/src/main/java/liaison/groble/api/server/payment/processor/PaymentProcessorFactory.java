package liaison.groble.api.server.payment.processor;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import liaison.groble.application.payment.exception.PaymentAuthenticationRequiredException;
import liaison.groble.common.context.UserContext;

import lombok.extern.slf4j.Slf4j;

/**
 * 결제 처리 전략을 선택하는 팩토리 클래스
 *
 * <p>UserContext에 따라 적절한 PaymentProcessor를 반환합니다.
 *
 * <p>Strategy 패턴의 Context 역할을 수행하며, 확장 가능한 구조를 제공합니다.
 */
@Slf4j
@Component
public class PaymentProcessorFactory {

  private final Map<String, PaymentProcessor> processorMap;

  public PaymentProcessorFactory(List<PaymentProcessor> processors) {
    this.processorMap =
        processors.stream()
            .collect(Collectors.toMap(PaymentProcessor::getSupportedUserType, Function.identity()));

    log.info("결제 처리 전략 등록 완료: {}", processorMap.keySet());
  }

  /**
   * 사용자 컨텍스트에 따른 적절한 결제 처리 전략을 반환
   *
   * @param userContext 사용자 컨텍스트
   * @return 결제 처리 전략
   */
  public PaymentProcessor getProcessor(UserContext userContext) {
    String userType = userContext.getUserType();
    PaymentProcessor processor = processorMap.get(userType);

    if (processor == null) {
      log.error("지원하지 않는 사용자 타입: {}", userType);
      throw PaymentAuthenticationRequiredException.forPayment();
    }

    return processor;
  }

  /**
   * 등록된 모든 PaymentProcessor 목록을 반환합니다. (디버깅/모니터링 용도)
   *
   * @return PaymentProcessor 목록
   */
  public List<PaymentProcessor> getAllProcessors() {
    return List.copyOf(processorMap.values());
  }
}
