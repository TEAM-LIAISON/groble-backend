package liaison.groble.domain.settlement.enums;

public enum SettlementType {
  DOCUMENT("자료형", 4), // 월 4회
  COACHING("서비스형", 2), // 월 2회
  LEGACY("기존방식", 1); // 하위호환용 - 월 1회

  private final String description;
  private final int cyclesPerMonth;

  SettlementType(String description, int cyclesPerMonth) {
    this.description = description;
    this.cyclesPerMonth = cyclesPerMonth;
  }
}
