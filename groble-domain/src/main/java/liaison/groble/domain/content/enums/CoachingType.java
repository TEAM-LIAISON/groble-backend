package liaison.groble.domain.content.enums;

public enum CoachingType {
  ONLINE("온라인"),
  OFFLINE("오프라인");

  private final String description;

  CoachingType(String description) {
    this.description = description;
  }
}
