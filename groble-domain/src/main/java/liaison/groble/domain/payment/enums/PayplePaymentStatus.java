package liaison.groble.domain.payment.enums;

import lombok.Getter;

@Getter
public enum PayplePaymentStatus {
  PENDING("대기중"),
  LINK_CREATED("링크생성됨"),
  BILLING_REGISTERED("빌링등록됨"),
  COMPLETED("완료"),
  FAILED("실패"),
  CANCELLED("취소됨");

  private final String description;

  PayplePaymentStatus(String description) {
    this.description = description;
  }
}
