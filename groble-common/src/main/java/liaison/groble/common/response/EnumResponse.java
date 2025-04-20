package liaison.groble.common.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EnumResponse {
  private String code;
  private String description;

  public static EnumResponse from(EnumWithDescription enumValue) {
    return new EnumResponse(enumValue.name(), enumValue.getDescription());
  }
}
