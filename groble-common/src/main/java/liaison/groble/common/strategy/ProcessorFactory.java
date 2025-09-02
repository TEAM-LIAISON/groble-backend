package liaison.groble.common.strategy;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import liaison.groble.common.context.UserContext;

import lombok.extern.slf4j.Slf4j;

/**
 * 제네릭 프로세서 팩토리 클래스
 *
 * <p>사용자 타입에 따라 적절한 처리 전략을 선택하는 팩토리의 공통 구현
 *
 * <p>Strategy 패턴의 Context 역할을 수행하며, 다양한 도메인에서 재사용 가능한 구조를 제공
 *
 * @param <T> UserTypeProcessor 구현하는 처리 전략 타입
 */
@Slf4j
public class ProcessorFactory<T extends UserTypeProcessor> {

  private final Map<String, T> processorMap;
  private final String domainName;
  private final Supplier<RuntimeException> exceptionSupplier;

  /**
   * ProcessorFactory 생성자
   *
   * @param processors 처리 전략 목록
   * @param domainName 도메인 이름 (로깅용)
   * @param exceptionSupplier 지원하지 않는 사용자 타입에 대한 예외 공급자
   */
  public ProcessorFactory(
      List<T> processors, String domainName, Supplier<RuntimeException> exceptionSupplier) {
    this.processorMap =
        processors.stream()
            .collect(
                Collectors.toMap(UserTypeProcessor::getSupportedUserType, Function.identity()));
    this.domainName = domainName;
    this.exceptionSupplier = exceptionSupplier;

    log.info("{} 처리 전략 등록 완료: {}", domainName, processorMap.keySet());
  }

  /**
   * 사용자 컨텍스트에 따른 적절한 처리 전략을 반환
   *
   * @param userContext 사용자 컨텍스트
   * @return 처리 전략
   * @throws RuntimeException 지원하지 않는 사용자 타입인 경우
   */
  public T getProcessor(UserContext userContext) {
    String userType = userContext.getUserType();
    T processor = processorMap.get(userType);

    if (processor == null) {
      log.error("지원하지 않는 사용자 타입: {} (도메인: {})", userType, domainName);
      throw exceptionSupplier.get();
    }

    return processor;
  }
}
