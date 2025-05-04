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

  // 개인사업자 유형인지 확인하는 헬퍼 메서드
  public boolean isIndividual() {
    return this == INDIVIDUAL_SIMPLIFIED || this == INDIVIDUAL_NORMAL;
  }

  // 간이과세자인지 확인하는 헬퍼 메서드
  public boolean isSimplifiedTax() {
    return this == INDIVIDUAL_SIMPLIFIED;
  }

  // 일반과세자인지 확인하는 헬퍼 메서드 (개인/법인 모두 포함)
  public boolean isNormalTax() {
    return this == INDIVIDUAL_NORMAL || this == CORPORATE;
  }
}
