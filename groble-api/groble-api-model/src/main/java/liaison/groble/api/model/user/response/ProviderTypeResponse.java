package liaison.groble.api.model.user.response;

import liaison.groble.common.response.EnumWithDescription;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "소셜 플랫폼 타입 Enum 응답")
public class ProviderTypeResponse {
  @Schema(description = "코드", example = "KAKAO")
  private String code;

  @Schema(description = "설명", example = "카카오")
  private String description;

  public static ProviderTypeResponse from(EnumWithDescription enumValue) {
    return new ProviderTypeResponse(enumValue.name(), enumValue.getDescription());
  }
}
