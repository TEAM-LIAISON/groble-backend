package liaison.groble.domain.purchase.enums;

public enum PurchaseStatus {
  PENDING("구매 대기"),
  COMPLETED("구매 완료"),
  CANCEL_REQUESTED("구매 취소 요청"),
  REFUND_REQUESTED("환불 요청"),
  REFUNDED("환불 완료"),
  FAILED("구매 실패");

  private final String description;

  PurchaseStatus(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }

  public boolean isActive() {
    return this == COMPLETED;
  }
}
