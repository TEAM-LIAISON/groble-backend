package liaison.groble.external.infotalk.dto.message;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * 메시지 발송 응답 DTO
 *
 * <p>중요: code 1000은 API 호출 성공을 의미하며, 실제 발송 성공을 의미하지 않습니다! 실제 발송 결과는 별도의 리포트 API를 통해 확인해야 합니다.
 */
@Data
public class MessageResponse {
  @JsonProperty("code")
  private int code; // 응답 코드 (1000: 성공)

  @JsonProperty("description")
  private String description; // 응답 설명

  @JsonProperty("refkey")
  private String refKey; // 고객사 메시지 키

  @JsonProperty("messagekey")
  private String messageKey; // 비즈뿌리오 메시지 키

  /**
   * API 호출이 성공했는지 확인
   *
   * @return 성공인 경우 true
   */
  public boolean isSuccess() {
    return code == 1000;
  }

  /**
   * 에러 메시지를 생성합니다
   *
   * @return 에러 메시지
   */
  public String getErrorMessage() {
    return String.format("Code: %d, Description: %s", code, description);
  }
}
