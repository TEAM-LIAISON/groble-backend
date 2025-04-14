package liaison.groble.common.response;

/** API 응답의 상태를 나타내는 열거형 */
public enum ResponseStatus {
  SUCCESS, // 성공
  ERROR, // 오류
  FAIL // 실패 (비즈니스 로직 실패)
}
