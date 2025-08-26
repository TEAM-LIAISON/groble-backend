package liaison.groble.domain.notification.event;

import lombok.Getter;

@Getter
public class UserSignUpEvent extends NotificationEvent {
  private final String userName;
  private final String phoneNumber;

  public UserSignUpEvent(Object source, Long userId, String userName, String phoneNumber) {
    super(source, userId);
    this.userName = userName;
    this.phoneNumber = phoneNumber;
  }
}
