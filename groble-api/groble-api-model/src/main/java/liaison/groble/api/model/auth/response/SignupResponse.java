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
public class SignupResponse {
  private String email;
  private boolean authenticated;

  public static SignupResponse of(String email) {
    return SignupResponse.builder().email(email).authenticated(true).build();
  }
}
