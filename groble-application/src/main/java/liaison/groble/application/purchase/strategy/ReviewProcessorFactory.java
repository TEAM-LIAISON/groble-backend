package liaison.groble.application.purchase.strategy;

import java.util.List;

import org.springframework.stereotype.Component;

import liaison.groble.application.purchase.exception.ReviewAuthenticationRequiredException;
import liaison.groble.common.context.UserContext;
import liaison.groble.common.strategy.ProcessorFactory;

/** 리뷰 처리 전략을 선택하는 팩토리 클래스 */
@Component
public class ReviewProcessorFactory {

  private final ProcessorFactory<ReviewProcessorStrategy> processorFactory;

  public ReviewProcessorFactory(List<ReviewProcessorStrategy> processors) {
    this.processorFactory =
        new ProcessorFactory<>(
            processors, "리뷰", ReviewAuthenticationRequiredException::forReviewAdd);
  }

  /**
   * 사용자 컨텍스트에 따른 적절한 리뷰 처리 전략을 반환
   *
   * @param userContext 사용자 컨텍스트
   * @return 리뷰 처리 전략
   */
  public ReviewProcessorStrategy getProcessor(UserContext userContext) {
    return processorFactory.getProcessor(userContext);
  }
}
