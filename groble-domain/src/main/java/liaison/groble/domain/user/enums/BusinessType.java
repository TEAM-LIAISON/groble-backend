package liaison.groble.domain.user.enums;

public enum BusinessType {
  INDIVIDUAL("개인사업자"),
  CORPORATE("법인사업자");

  private final String displayName;

  BusinessType(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }
}
