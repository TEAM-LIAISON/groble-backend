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
public class SocialSignUpResponse {

  @Schema(description = "사용자가 기입한 닉네임", example = "동민동민")
  private String nickname;

  @Schema(description = "회원가입 성공 여부", example = "true")
  private boolean authenticated;

  public static SocialSignUpResponse of(String nickname) {
    return SocialSignUpResponse.builder().nickname(nickname).authenticated(true).build();
  }
}
