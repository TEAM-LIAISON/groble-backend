package liaison.groble.application.guest.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GuestAuthVerifyDTO {
  private String username;
  private String email;
  private String phoneNumber;
  private String authCode;
}
