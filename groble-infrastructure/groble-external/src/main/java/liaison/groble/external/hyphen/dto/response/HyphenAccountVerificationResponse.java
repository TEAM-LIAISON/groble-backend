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
public class HyphenAccountVerificationResponse {

  @JsonProperty("name")
  private String name; // 예금주명

  @JsonProperty("reply")
  private String reply; // 응답코드

  @JsonProperty("reply_msg")
  private String replyMsg; // 응답메시지

  // 성공 여부 확인
  public boolean isSuccess() {
    return "00".equals(reply) || "000".equals(reply);
  }
}
