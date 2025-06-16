package liaison.groble.application.auth.dto;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SocialBasicInfoDto {
  private String userType;
  private List<String> termsTypeStrings;
}
