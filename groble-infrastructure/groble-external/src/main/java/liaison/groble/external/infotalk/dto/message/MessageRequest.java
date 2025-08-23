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

  // ===== 선택 파라미터 =====

  @JsonProperty("country")
  private String country; // 국가코드 (기본값: 82)

  @JsonProperty("userinfo")
  private String userInfo; // 정산용 부서코드

  @JsonProperty("sendtime")
  private String sendTime; // 예약발송 시간 (YYYYMMDDHHmmss)

  // ===== 알림톡/친구톡 전용 =====

  @JsonProperty("senderkey")
  private String senderKey; // 발신프로필키 (카카오톡 채널)

  @JsonProperty("templatecode")
  private String templateCode; // 템플릿 코드 (사전 승인 필요)

  @JsonProperty("button")
  private List<ButtonInfo> buttons; // 버튼 정보

  // ===== MMS 전용 =====

  @JsonProperty("file")
  private List<FileInfo> files; // 첨부파일 정보

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

    @JsonProperty("header")
    private String header; // 선택 필드

    @JsonProperty("title")
    private String title;

    @JsonProperty("button")
    private List<ButtonInfo> button; // 기존 ButtonInfo 재사용

    @JsonProperty("item")
    private ItemInfo item; // 아이템 리스트용

    @JsonProperty("itemhighlight")
    private ItemHighlight itemhighlight; // 아이템 하이라이트용
  }

  // 추가 내부 클래스들
  @Data
  @Builder
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class ItemInfo {
    @JsonProperty("list")
    private List<ItemDetail> list;

    @JsonProperty("summary")
    private ItemSummary summary;
  }

  @Data
  @Builder
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class ItemDetail {
    @JsonProperty("title")
    private String title;

    @JsonProperty("description")
    private String description;
  }

  @Data
  @Builder
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class ItemSummary {
    @JsonProperty("title")
    private String title;

    @JsonProperty("description")
    private String description;
  }

  @Data
  @Builder
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class ItemHighlight {
    @JsonProperty("title")
    private String title;

    @JsonProperty("description")
    private String description;
  }
}
