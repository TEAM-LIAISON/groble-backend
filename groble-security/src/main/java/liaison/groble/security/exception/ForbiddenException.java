package liaison.groble.security.exception;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ResponseStatus;

/** 인증은 되었으나 권한이 없는 경우 발생하는 예외 HTTP 상태 코드 403(Forbidden)을 반환 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class ForbiddenException extends AccessDeniedException {

  private static final long serialVersionUID = 1L;

  /**
   * 기본 생성자
   *
   * @param message 예외 메시지
   */
  public ForbiddenException(String message) {
    super(message);
  }

  /**
   * 원인 예외와 함께 생성하는 생성자
   *
   * @param message 예외 메시지
   * @param cause 원인 예외
   */
  public ForbiddenException(String message, Throwable cause) {
    super(message, cause);
  }
}
