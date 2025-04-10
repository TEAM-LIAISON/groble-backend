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
public class SignInResponse {
  private String email;
  private boolean authenticated;
  private String userType;
  private String nextRoutePath;

  public static SignInResponse of(String email, String userType, String nextRoutePath) {
    return SignInResponse.builder()
        .email(email)
        .authenticated(true)
        .userType(userType)
        .nextRoutePath(nextRoutePath)
        .build();
  }
}
