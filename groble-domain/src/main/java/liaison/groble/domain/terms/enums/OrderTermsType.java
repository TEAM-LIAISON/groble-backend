package liaison.groble.domain.terms.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderTermsType {
  PERSONAL_INFO_COLLECTION_AND_THIRD_PARTY_PROVISION("개인정보 수집 및 제3자 제공", true),
  TERMS_OF_SERVICE("서비스 이용약관", true),
  REFUND_POLICY("환불 규정", true),
  MARKETPLACE_INTERMEDIARY_NOTICE("그로블은 통신판매중개자이며 상품·서비스의 제공 및 책임은 판매자에게 있습니다", true);

  private final String description;
  private final boolean required;

  // 필요시 description 기반 매핑 메서드 추가
  public static OrderTermsType fromDescription(String description) {
    for (OrderTermsType type : values()) {
      if (type.getDescription().equals(description)) {
        return type;
      }
    }
    throw new IllegalArgumentException("설명에 해당하는 OrderTermsType이 존재하지 않습니다: " + description);
  }
}
