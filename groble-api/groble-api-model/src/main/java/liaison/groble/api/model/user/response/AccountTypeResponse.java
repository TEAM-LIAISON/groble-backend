package liaison.groble.api.model.user.response;

import liaison.groble.common.response.EnumWithDescription;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "계정 타입 Enum 응답")
public class AccountTypeResponse {
  @Schema(description = "코드", example = "INTEGRATED")
  private String code;

  @Schema(description = "설명", example = "통합 계정")
  private String description;

  public static AccountTypeResponse from(EnumWithDescription enumValue) {
    return new AccountTypeResponse(enumValue.name(), enumValue.getDescription());
  }
}
