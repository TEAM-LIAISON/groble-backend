package liaison.groble.domain.user.enums;

public enum BankAccountVerificationStatus {
  PENDING("대기 중"),
  REQUESTED("요청됨"),
  PROCESSING("처리 중"),
  COMPLETED("완료됨"),
  FAILED("실패"),
  EXPIRED("만료됨");

  private final String description;

  BankAccountVerificationStatus(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }
}
