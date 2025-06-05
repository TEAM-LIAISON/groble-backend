package liaison.groble.external.hyphen.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class HyphenOneWonVerifyResponse {

  @JsonProperty("successYn")
  private String successYn; // 성공여부(Y:성공, N:실패)

  @JsonProperty("error")
  private String error; // 에러메세지

  // 성공 여부 확인
  public boolean isSuccess() {
    return "Y".equals(successYn);
  }
}
