package liaison.groble.domain.product.enums;

public enum GigType {
  COACHING("코칭"),
  DOCUMENT("자료");

  private final String description;

  GigType(String description) {
    this.description = description;
  }
}
