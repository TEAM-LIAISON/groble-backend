package liaison.groble.external.infotalk.dto.message;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

/**
 * 메시지 발송 요청 DTO @JsonInclude(JsonInclude.Include.NON_NULL) 사용하여 null 값은 JSON에 포함되지 않도록 합니다. 이는 선택적
 * 파라미터를 처리하는 깔끔한 방법입니다.
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageRequest {
  // ===== 필수 파라미터 =====

  @JsonProperty("account")
  private String account; // 비즈뿌리오 계정

  @JsonProperty("type")
  private String type; // 메시지 타입 (sms, lms, mms, at, ft)

  @JsonProperty("from")
  private String from; // 발신번호 (사전 등록된 번호만 가능)

  @JsonProperty("to")
  private String to; // 수신번호 (하이픈 제외)

  @JsonProperty("content")
  private Object content; // String 또는 AtContent 객체 (하나로 통합)

  @JsonProperty("refkey")
  private String refKey; // 고객사 메시지 고유키 (중복 발송 방지용)

  // 알림톡용 내부 클래스들
  @Data
  @Builder
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class AtContent {
    @JsonProperty("at")
    private AtMessage at;
  }

  @Data
  @Builder
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class AtMessage {
    @JsonProperty("message")
    private String message;

    @JsonProperty("senderkey")
    private String senderkey;

    @JsonProperty("templatecode")
    private String templatecode;

    @JsonProperty("title")
    private String title;

    @JsonProperty("button")
    private List<ButtonInfo> button; // 기존 ButtonInfo 재사용
  }
}
