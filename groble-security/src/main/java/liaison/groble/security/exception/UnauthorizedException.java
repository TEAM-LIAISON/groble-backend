package liaison.groble.security.exception;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ResponseStatus;

/** 인증되지 않은 사용자가 접근했을 때 발생하는 예외 HTTP 상태 코드 401(Unauthorized)을 반환 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class UnauthorizedException extends AuthenticationException {

  private static final long serialVersionUID = 1L;

  /**
   * 기본 생성자
   *
   * @param message 예외 메시지
   */
  public UnauthorizedException(String message) {
    super(message);
  }
}
