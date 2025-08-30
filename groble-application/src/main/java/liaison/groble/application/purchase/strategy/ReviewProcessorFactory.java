package liaison.groble.application.purchase.strategy;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import liaison.groble.application.purchase.exception.ReviewAuthenticationRequiredException;
import liaison.groble.common.context.UserContext;

import lombok.extern.slf4j.Slf4j;

/** 리뷰 처리 전략을 선택하는 팩토리 클래스 */
@Slf4j
@Component
public class ReviewProcessorFactory {

  private final Map<String, ReviewProcessorStrategy> processorMap;

  public ReviewProcessorFactory(List<ReviewProcessorStrategy> processors) {
    this.processorMap =
        processors.stream()
            .collect(
                Collectors.toMap(
                    ReviewProcessorStrategy::getSupportedUserType, Function.identity()));

    log.info("리뷰 처리 전략 등록 완료: {}", processorMap.keySet());
  }

  /**
   * 사용자 컨텍스트에 따른 적절한 리뷰 처리 전략을 반환
   *
   * @param userContext 사용자 컨텍스트
   * @return 리뷰 처리 전략
   */
  public ReviewProcessorStrategy getProcessor(UserContext userContext) {
    String userType = userContext.getUserType();
    ReviewProcessorStrategy processor = processorMap.get(userType);

    if (processor == null) {
      log.error("지원하지 않는 사용자 타입: {}", userType);
      throw ReviewAuthenticationRequiredException.forReviewAdd();
    }

    return processor;
  }
}
