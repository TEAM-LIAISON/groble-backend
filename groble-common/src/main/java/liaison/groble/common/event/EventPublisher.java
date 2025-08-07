package liaison.groble.common.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 애플리케이션 이벤트 발행자 */
@Component
@RequiredArgsConstructor
public class EventPublisher {
  private final ApplicationEventPublisher applicationEventPublisher;

  /**
   * 이벤트 발행
   *
   * @param event 발행할 이벤트
   */
  public void publish(Object event) {
    applicationEventPublisher.publishEvent(event);
  }
}
