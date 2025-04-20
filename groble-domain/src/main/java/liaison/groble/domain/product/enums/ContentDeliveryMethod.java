package liaison.groble.domain.product.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ContentDeliveryMethod {
  IMMEDIATE_DOWNLOAD("즉시 다운로드"),
  FUTURE_UPLOAD("추후 업로드");

  private final String description;
}
