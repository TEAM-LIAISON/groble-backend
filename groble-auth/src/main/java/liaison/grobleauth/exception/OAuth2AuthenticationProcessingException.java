package liaison.grobleauth.exception;

import org.springframework.security.core.AuthenticationException;

public class OAuth2AuthenticationProcessingException extends AuthenticationException {
  private static final long serialVersionUID = 1L;

  /**
   * 예외 메시지와 함께 OAuth2 인증 처리 예외를 생성합니다.
   *
   * @param msg 예외 메시지
   */
  public OAuth2AuthenticationProcessingException(String msg) {
    super(msg);
  }

  /**
   * 예외 메시지와 원인 예외와 함께 OAuth2 인증 처리 예외를 생성합니다.
   *
   * @param msg 예외 메시지
   * @param cause 원인 예외
   */
  public OAuth2AuthenticationProcessingException(String msg, Throwable cause) {
    super(msg, cause);
  }
}
