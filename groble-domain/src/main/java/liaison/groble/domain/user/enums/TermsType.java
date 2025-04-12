package liaison.groble.domain.user.enums;

import lombok.Getter;

@Getter
public enum TermsType {
  AGE_POLICY("만 14세 이상입니다.", true),
  PRIVACY_POLICY("개인정보 수집 및 이용 동의", true),
  SERVICE_TERMS("서비스 이용약관 동의", true),
  SALES_TERMS("판매 이용약관 동의", true),
  MARKETING("마케팅 활용 동의", false),
  ADVERTISING("광고성 정보 수신 동의", false);

  private final String description;
  private final boolean required;

  TermsType(String description, boolean required) {
    this.description = description;
    this.required = required;
  }
}
