package liaison.groble.domain.user.enums;

import lombok.Getter;

@Getter
public enum BusinessType {
  INDIVIDUAL_SIMPLIFIED("개인사업자(간이과세자)"),
  INDIVIDUAL_NORMAL("개인사업자(일반과세자)"),
  CORPORATE("법인사업자");

  private final String displayName;

  BusinessType(String displayName) {
    this.displayName = displayName;
  }
}
