package liaison.groble.domain.user.enums;

public enum BankAccountVerificationType {
  ONE_CENT_DEPOSIT("1원 입금 인증"),
  INSTANT("실시간 계좌 인증"),
  ACCOUNT_OWNER("예금주 조회");

  private final String description;

  BankAccountVerificationType(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }
}
