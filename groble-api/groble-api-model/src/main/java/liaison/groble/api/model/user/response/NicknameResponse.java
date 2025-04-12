package liaison.groble.api.model.user.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NicknameResponse {
  private String nickname;

  public static NicknameResponse of(String nickname) {
    return new NicknameResponse(nickname);
  }
}
