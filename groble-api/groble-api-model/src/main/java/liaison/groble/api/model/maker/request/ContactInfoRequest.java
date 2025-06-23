package liaison.groble.api.model.maker.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContactInfoRequest {
  @Schema(
      description = "인스타그램 링크",
      example = "https://www.instagram.com/dongmin",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String instagram;

  @Schema(
      description = "이메일",
      example = "groble@example.com",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String email;

  @Schema(
      description = "오픈채팅 링크",
      example = "https://open.kakao.com/o/groble",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String openChat;

  @Schema(
      description = "그 외 링크",
      example = "https://example.com/other",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String etc;
}
