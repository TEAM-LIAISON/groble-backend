package liaison.groble.application.purchase.strategy;

import java.util.List;

import org.springframework.stereotype.Component;

import liaison.groble.application.purchase.exception.PurchaseAuthenticationRequiredException;
import liaison.groble.common.context.UserContext;
import liaison.groble.common.strategy.ProcessorFactory;

@Component
public class PurchaseProcessorFactory {
  private final ProcessorFactory<PurchaseProcessorStrategy> processorFactory;

  public PurchaseProcessorFactory(List<PurchaseProcessorStrategy> processors) {
    this.processorFactory =
        new ProcessorFactory<>(
            processors, "구매", PurchaseAuthenticationRequiredException::forPurchaseList);
  }

  /**
   * 사용자 컨텍스트에 따른 적절한 구매 처리 전략을 반환
   *
   * @param userContext 사용자 컨텍스트
   * @return 구매 처리 전략
   */
  public PurchaseProcessorStrategy getProcessor(UserContext userContext) {
    return processorFactory.getProcessor(userContext);
  }
}
