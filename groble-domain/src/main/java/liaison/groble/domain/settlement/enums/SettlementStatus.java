package liaison.groble.domain.settlement.enums;

public enum SettlementStatus {
  PENDING("정산 예정"), // 정산이 예정된 상태 (아직 정산 마감 날짜 안지남)
  PROCESSING("정산 처리중"), // 정산이 진행 중인 상태 (정산 마감 날짜 지남)
  COMPLETED("정산 완료"), // 정산이 완료된 상태  (정산 완료)
  ON_HOLD("정산 보류"), // 정산이 보류된 상태 (정산 보류)
  CANCELLED("정산 취소"); // 정산 취소된 상태 (정산 취소 -> 지급 내역 롤백)

  private final String description;

  SettlementStatus(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }
}
