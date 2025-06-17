package liaison.groble.application.user.dto;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SocialBasicInfoDTO {
  private String userType;
  private List<String> termsTypeStrings;
}
