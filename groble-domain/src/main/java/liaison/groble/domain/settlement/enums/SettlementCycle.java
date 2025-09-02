package liaison.groble.domain.settlement.enums;

public enum SettlementCycle {
  MONTHLY("월1회", 1), // 기존 방식
  BIMONTHLY("월2회", 2), // 서비스형
  WEEKLY("주단위", 4); // 자료형 (실제로는 주가 아니지만 4회)

  private final String description;
  private final int frequency;

  SettlementCycle(String description, int frequency) {
    this.description = description;
    this.frequency = frequency;
  }
}
