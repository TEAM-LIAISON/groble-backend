package liaison.groble.application.hometest.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HomeTestVerifyAuthDTO {
  private final String phoneNumber;
  private final String authCode;
  private final String nickname;
  private final String email;
}
