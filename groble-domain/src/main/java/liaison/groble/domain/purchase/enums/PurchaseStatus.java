package liaison.groble.domain.purchase.enums;

public enum PurchaseStatus {
  PENDING("구매대기"),
  PAID("구매완료"),
  CANCELLED("구매취소"),
  REFUND_REQUESTED("환불요청"),
  REFUNDED("환불완료"),
  FAILED("구매실패");

  private final String description;

  PurchaseStatus(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }

  public boolean isActive() {
    return this == PAID;
  }

  public boolean isTerminated() {
    return this == CANCELLED || this == REFUNDED || this == FAILED;
  }
}
