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
  private boolean hasAgreedToTerms;
  private boolean hasNickname;
  private boolean hasVerifiedPhoneNumber;
  private boolean authenticated;
}
