package liaison.groble.application.auth.dto;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SocialSignUpDto {

  private String userType;
  private List<String> termsTypeStrings;
  private String nickname;
  private String phoneNumber;
}
