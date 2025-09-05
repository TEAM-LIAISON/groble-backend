package liaison.groble.application.guest.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UpdateGuestUserInfoDTO {
  private String email;
  private String username;
}
