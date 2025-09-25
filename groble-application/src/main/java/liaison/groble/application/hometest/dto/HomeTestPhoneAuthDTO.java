package liaison.groble.application.hometest.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HomeTestPhoneAuthDTO {
  private final String phoneNumber;
}
