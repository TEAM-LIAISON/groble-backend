package liaison.groble.common.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "Enum 공통 응답 (코드 + 설명)")
public class EnumResponse {

  @Schema(description = "코드 값", example = "BUYER")
  private String code;

  @Schema(description = "설명", example = "구매자")
  private String description;

  public static EnumResponse from(EnumWithDescription enumValue) {
    return new EnumResponse(enumValue.name(), enumValue.getDescription());
  }
}
