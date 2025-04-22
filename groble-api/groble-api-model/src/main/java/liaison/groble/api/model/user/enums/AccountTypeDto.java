package liaison.groble.api.model.user.enums;

import liaison.groble.common.response.EnumWithDescription;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AccountTypeDto implements EnumWithDescription {
  INTEGRATED("통합 계정"),
  SOCIAL("소셜 계정");

  private final String description;
}
