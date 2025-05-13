package liaison.groble.application.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DeprecatedSignUpDto {
  private String email;
  private String password;
}
