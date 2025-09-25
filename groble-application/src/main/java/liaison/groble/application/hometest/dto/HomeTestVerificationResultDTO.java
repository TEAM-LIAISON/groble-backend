package liaison.groble.application.hometest.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HomeTestVerificationResultDTO {
  private final String phoneNumber;
  private final String nickname;
  private final String email;
}
