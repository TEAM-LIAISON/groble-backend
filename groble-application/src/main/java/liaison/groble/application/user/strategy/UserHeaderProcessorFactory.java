package liaison.groble.application.user.strategy;

import java.util.List;

import org.springframework.stereotype.Component;

import liaison.groble.application.user.exception.UserAuthenticationRequiredException;
import liaison.groble.common.context.UserContext;
import liaison.groble.common.strategy.ProcessorFactory;

/**
 * 사용자 헤더 정보 처리 전략 팩토리
 *
 * <p>Order 패키지의 OrderProcessorFactory와 동일한 구조로 설계되었습니다.
 */
@Component
public class UserHeaderProcessorFactory {
  private final ProcessorFactory<UserHeaderStrategy> processorFactory;

  public UserHeaderProcessorFactory(List<UserHeaderStrategy> processors) {
    this.processorFactory =
        new ProcessorFactory<>(
            processors, "사용자 헤더", UserAuthenticationRequiredException::forUserHeaderInform);
  }

  /**
   * 사용자 컨텍스트에 따른 적절한 사용자 헤더 처리 전략을 반환
   *
   * @param userContext 사용자 컨텍스트
   * @return 사용자 헤더 처리 전략
   */
  public UserHeaderStrategy getProcessor(UserContext userContext) {
    return processorFactory.getProcessor(userContext);
  }
}
