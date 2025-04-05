package liaison.groble.application.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SignUpDto {
  private String email;
  private String password;
}
