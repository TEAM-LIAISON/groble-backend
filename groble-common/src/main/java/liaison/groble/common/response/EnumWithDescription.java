package liaison.groble.common.response;

/** Enum이 code + description 구조로 내려갈 수 있도록 강제하는 인터페이스 */
public interface EnumWithDescription {
  String name();

  String getDescription();
}
