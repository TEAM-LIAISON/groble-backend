package liaison.groble.domain.content.enums;

public enum ContentType {
  COACHING("코칭"),
  DOCUMENT("자료");

  private final String description;

  ContentType(String description) {
    this.description = description;
  }
}
