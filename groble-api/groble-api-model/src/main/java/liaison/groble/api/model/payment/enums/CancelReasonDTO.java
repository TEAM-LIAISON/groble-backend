package liaison.groble.api.model.payment.enums;

public enum CancelReasonDTO {
  OTHER_PAYMENT_METHOD("다른 수단으로 결제할게요"),
  CHANGED_MIND("마음이 바뀌었어요"),
  FOUND_CHEAPER_CONTENT("더 저렴한 콘텐츠를 찾았어요"),
  ETC("기타");

  private final String description;

  CancelReasonDTO(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }
}
