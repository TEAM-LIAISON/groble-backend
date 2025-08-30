package liaison.groble.application.purchase.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 리뷰 작성/수정/삭제 시 적절한 인증이 필요할 때 발생하는 예외 userId 또는 guestUserId 중 하나는 반드시 제공되어야 하는 상황에서 사용 HTTP 상태 코드
 * 400(Bad Request)을 반환
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ReviewAuthenticationRequiredException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  /** 기본 메시지로 예외 생성 */
  public ReviewAuthenticationRequiredException() {
    super("리뷰 작성을 위해서는 회원 로그인 또는 비회원 인증이 필요합니다.");
  }

  /**
   * 사용자 정의 메시지로 예외 생성
   *
   * @param message 예외 메시지
   */
  public ReviewAuthenticationRequiredException(String message) {
    super(message);
  }

  /** 리뷰 작성용 예외 메시지 */
  public static ReviewAuthenticationRequiredException forReviewAdd() {
    return new ReviewAuthenticationRequiredException(
        "리뷰 작성을 위해서는 userId 또는 guestUserId 중 하나는 반드시 제공되어야 합니다.");
  }

  /** 리뷰 수정/삭제용 예외 메시지 */
  public static ReviewAuthenticationRequiredException forReviewUpdate() {
    return new ReviewAuthenticationRequiredException(
        "리뷰 수정을 위해서는 userId 또는 guestUserId 중 하나는 반드시 제공되어야 합니다.");
  }
}
