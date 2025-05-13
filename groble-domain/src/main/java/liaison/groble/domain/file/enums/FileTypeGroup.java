package liaison.groble.domain.file.enums;

public enum FileTypeGroup {
  IMAGE("images"),
  CONTENT("contents"),
  LICENSE("business-license"),
  DEFAULT("default");

  private final String dir;

  FileTypeGroup(String dir) {
    this.dir = dir;
  }

  public String getDir() {
    return dir;
  }

  public static FileTypeGroup from(String type) {
    if (type == null) return DEFAULT;

    return switch (type.toUpperCase()) {
      case "IMAGE" -> IMAGE;
      case "DOCUMENT", "PDF", "WORD", "EXCEL", "PPT" -> CONTENT;
      case "LICENSE" -> LICENSE;
      default -> DEFAULT;
    };
  }
}
