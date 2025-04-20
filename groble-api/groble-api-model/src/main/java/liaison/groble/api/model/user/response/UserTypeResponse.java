package liaison.groble.api.model.user.response;

import liaison.groble.common.response.EnumWithDescription;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "사용자 유형 Enum 응답")
public class UserTypeResponse {
  @Schema(description = "코드", example = "BUYER")
  private String code;

  @Schema(description = "설명", example = "구매자")
  private String description;

  public static UserTypeResponse from(EnumWithDescription enumValue) {
    return new UserTypeResponse(enumValue.name(), enumValue.getDescription());
  }
}
