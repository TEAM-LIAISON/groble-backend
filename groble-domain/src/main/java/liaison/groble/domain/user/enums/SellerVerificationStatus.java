package liaison.groble.domain.user.enums;

public enum SellerVerificationStatus {
  PENDING("인증 필요", "판매자 등록을 위해 인증이 필요합니다"),
  IN_PROGRESS("인증 진행 중", "인증 절차가 진행 중입니다"),
  FAILED("인증 실패", "인증에 실패했습니다"),
  VERIFIED("인증 완료", "판매자 인증이 완료되었습니다");

  private final String displayName;
  private final String description;

  SellerVerificationStatus(String displayName, String description) {
    this.displayName = displayName;
    this.description = description;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getDescription() {
    return description;
  }
}
