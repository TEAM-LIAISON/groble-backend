package liaison.groble.api.model.user.enums;

import liaison.groble.common.response.EnumWithDescription;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserTypeDto implements EnumWithDescription {
  BUYER("구매자"),
  SELLER("판매자");

  private final String description;
}
