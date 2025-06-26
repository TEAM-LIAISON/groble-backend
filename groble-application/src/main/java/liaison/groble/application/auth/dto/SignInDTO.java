package liaison.groble.application.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SignInDTO {
  private String email;
  private String password;
}
