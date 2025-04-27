package liaison.groble.domain.gig.enums;

public enum GigType {
  COACHING("코칭"),
  DOCUMENT("자료");

  private final String description;

  GigType(String description) {
    this.description = description;
  }
}
