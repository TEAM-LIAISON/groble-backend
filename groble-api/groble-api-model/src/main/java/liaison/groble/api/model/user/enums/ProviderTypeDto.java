package liaison.groble.api.model.user.enums;

import liaison.groble.common.response.EnumWithDescription;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProviderTypeDto implements EnumWithDescription {
  GOOGLE("구글"),
  KAKAO("카카오"),
  NAVER("네이버");

  private final String description;
}
