package liaison.groble.domain.user.enums;

public enum IdentityVerificationStatus {
  NONE("미인증"),
  REQUESTED("인증 요청됨"),
  IN_PROGRESS("인증 진행 중"),
  VERIFIED("인증됨"),
  REJECTED("거부됨"),
  EXPIRED("만료됨"),
  FAILED("실패");

  private final String description;

  IdentityVerificationStatus(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }
}
