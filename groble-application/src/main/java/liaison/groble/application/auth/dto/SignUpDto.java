package liaison.groble.application.auth.dto;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SignUpDto {
  private String userType;
  private List<String> termsTypeStrings;
  private String email;
  private String password;
  private String nickname;
}
