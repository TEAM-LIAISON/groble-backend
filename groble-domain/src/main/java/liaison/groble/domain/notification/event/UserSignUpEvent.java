package liaison.groble.domain.notification.event;

import lombok.Getter;

@Getter
public class UserSignUpEvent extends NotificationEvent {
  private final String username;
  private final String phoneNumber;

  public UserSignUpEvent(Object source, Long userId, String username, String phoneNumber) {
    super(source, userId);
    this.username = username;
    this.phoneNumber = phoneNumber;
  }
}
