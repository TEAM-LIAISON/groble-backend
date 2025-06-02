package liaison.groble.api.model.auth.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SignInTestResponse {
  private String email;
  private boolean authenticated;
  private String userType;
  private String nextRoutePath;
  private String accessToken;
  private String refreshToken;

  public static SignInTestResponse of(
      String email,
      String userType,
      String nextRoutePath,
      String accessToken,
      String refreshToken) {
    return SignInTestResponse.builder()
        .email(email)
        .authenticated(true)
        .userType(userType)
        .nextRoutePath(nextRoutePath)
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .build();
  }
}
