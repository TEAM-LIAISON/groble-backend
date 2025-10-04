package liaison.groble.application.hometest.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HomeTestSaveEmailDTO {
  private final String phoneNumber;
  private final String email;
}
