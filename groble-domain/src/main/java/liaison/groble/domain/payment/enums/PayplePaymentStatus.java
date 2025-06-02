package liaison.groble.domain.payment.enums;

import lombok.Getter;

@Getter
public enum PayplePaymentStatus {
  PENDING("대기중"),
  COMPLETED("완료"),
  FAILED("실패"),
  CANCELLED("취소됨");

  private final String description;

  PayplePaymentStatus(String description) {
    this.description = description;
  }
}
