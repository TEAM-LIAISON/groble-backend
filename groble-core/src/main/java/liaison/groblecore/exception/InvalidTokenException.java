package liaison.groblecore.exception;

/** 유효하지 않은, 만료된, 또는 존재하지 않는 토큰에 대한 예외 이메일 인증, 비밀번호 재설정 등 토큰 기반 인증 처리 시 발생 */
public class InvalidTokenException extends RuntimeException {

  public InvalidTokenException() {
    super("유효하지 않은 토큰입니다.");
  }

  public InvalidTokenException(String message) {
    super(message);
  }

  public InvalidTokenException(String message, Throwable cause) {
    super(message, cause);
  }
}
