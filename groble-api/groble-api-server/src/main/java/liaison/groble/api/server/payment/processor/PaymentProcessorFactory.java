package liaison.groble.api.server.payment.processor;

import java.util.List;

import org.springframework.stereotype.Component;

import liaison.groble.application.payment.exception.PaymentAuthenticationRequiredException;
import liaison.groble.common.model.Accessor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * PaymentProcessor 팩토리
 *
 * <p>사용자 타입(Accessor 상태)에 따라 적절한 PaymentProcessor를 반환합니다. Strategy 패턴의 Context 역할을 수행하며, 확장 가능한 구조를
 * 제공합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentProcessorFactory {

  private final List<PaymentProcessor> processors;

  /**
   * 사용자 타입에 맞는 PaymentProcessor를 반환합니다.
   *
   * @param accessor 사용자 정보
   * @return 적절한 PaymentProcessor
   * @throws PaymentAuthenticationRequiredException 지원되지 않는 사용자 타입인 경우
   */
  public PaymentProcessor getProcessor(Accessor accessor) {
    log.debug(
        "PaymentProcessor 선택 중 - isAuthenticated: {}, isGuest: {}",
        accessor.isAuthenticated(),
        accessor.isGuest());

    return processors.stream()
        .filter(processor -> processor.supports(accessor))
        .findFirst()
        .orElseThrow(
            () -> {
              log.warn(
                  "지원되지 않는 사용자 타입 - isAuthenticated: {}, isGuest: {}",
                  accessor.isAuthenticated(),
                  accessor.isGuest());
              return PaymentAuthenticationRequiredException.forPayment();
            });
  }

  /**
   * 등록된 모든 PaymentProcessor 목록을 반환합니다. (디버깅/모니터링 용도)
   *
   * @return PaymentProcessor 목록
   */
  public List<PaymentProcessor> getAllProcessors() {
    return List.copyOf(processors);
  }
}
