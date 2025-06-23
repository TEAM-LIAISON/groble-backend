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
public class MarketEditRequest {

  @Schema(
      description = "마켓 이름",
      example = "동민's 마켓",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String marketName;

  @Schema(
      description = "프로필 이미지 경로",
      example = "https://example.com/profile.jpg",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String profileImageUrl;

  @Schema(
      description = "groble.im/ 뒤에 붙는 메이커만의 마켓 링크 URL",
      example = "dongmin",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String marketLinkUrl;

  @Schema(description = "문의하기 (연락처 수단) 정보 객체", implementation = ContactInfoRequest.class)
  private ContactInfoRequest contactInfo;

  @Schema(
      description = "대표 콘텐츠 ID",
      example = "1",
      type = "integer",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private Long representativeContentId;
}
