package liaison.groble.domain.terms.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderTermsType {
  ELECTRONIC_FINANCIAL("전자금융거래 이용약관에 동의", true),
  PURCHASE_POLICY("결제 진행 및 구매조건에 동의", true),
  PERSONAL_INFORMATION("개인정보 수집 및 제3자 제공에 동의", true);

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
