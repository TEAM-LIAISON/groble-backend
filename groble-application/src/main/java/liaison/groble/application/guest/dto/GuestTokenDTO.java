package liaison.groble.application.guest.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GuestTokenDTO {
  private String phoneNumber;
  private String email;
  private String username;
  private String guestToken;
  private boolean authenticated;
  private boolean hasCompleteUserInfo;
}
