package liaison.groble.application.user.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** 401(Unauthorized)을 반환 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class UserAuthenticationRequiredException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  /** 기본 메시지로 예외 생성 */
  public UserAuthenticationRequiredException() {
    super("사용자 조회를 위해서 회원 로그인 또는 비회원 전화번호 인증이 필요합니다.");
  }

  /**
   * 사용자 정의 메시지로 예외 생성
   *
   * @param message 예외 메시지
   */
  public UserAuthenticationRequiredException(String message) {
    super(message);
  }

  /** 주문 생성용 예외 메시지 */
  public static UserAuthenticationRequiredException forUserHeaderInform() {
    return new UserAuthenticationRequiredException(
        "사용자 프로필 조회를 위해서는 회원 로그인 또는 비회원 전화번호 인증이 필요합니다.");
  }
}
