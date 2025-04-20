package liaison.groble.domain.product.enums;

public enum ContentType {
  COACHING("코칭"),
  DOCUMENT("자료");

  private final String description;

  ContentType(String description) {
    this.description = description;
  }
}
