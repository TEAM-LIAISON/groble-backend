package liaison.groble.api.model.auth.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SocialBasicInfoResponse {

  @Schema(description = "회원가입을 진행한 이메일", example = "email@email.com")
  private String email;

  @Schema(description = "회원가입 성공 여부", example = "true")
  private boolean authenticated;

  public static SocialBasicInfoResponse of(String email) {
    return SocialBasicInfoResponse.builder().email(email).authenticated(true).build();
  }
}
