package liaison.groble.domain.notification.event;

import org.springframework.context.ApplicationEvent;

import lombok.Getter;

@Getter
public class NotificationEvent extends ApplicationEvent {
  private final Long userId;

  // protected -> 같은 패키지나 하위 클래스에서만 생성할 수 있다.
  protected NotificationEvent(Object source, Long userId) {
    super(source); // source: 이벤트 발생 주체
    this.userId = userId;
  }
}
