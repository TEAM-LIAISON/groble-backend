package liaison.groble.external.infotalk.dto.message;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

/**
 * 버튼 정보 DTO
 *
 * <p>알림톡/친구톡에서 사용하는 버튼입니다. 웹링크(WL), 앱링크(AL), 봇키워드(BK) 등 다양한 타입을 지원합니다.
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ButtonInfo {
  @JsonProperty("name")
  private String name; // 버튼명

  @JsonProperty("type")
  private String type; // 버튼 타입 (WL, AL, BK, MD, BC)

  @JsonProperty("url_mobile")
  private String urlMobile; // 모바일 URL

  @JsonProperty("url_pc")
  private String urlPc; // PC URL (선택)

  @JsonProperty("scheme")
  private String scheme; // 앱 스킴 (앱링크용)
}
