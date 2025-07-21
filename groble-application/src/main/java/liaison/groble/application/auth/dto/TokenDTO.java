package liaison.groble.application.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TokenDTO {
  private String accessToken;
  private String refreshToken;
  private long accessTokenExpiresIn;
}
