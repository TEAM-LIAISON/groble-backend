package liaison.groble.application.purchase.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** 콘텐츠를 구매하지 않은 사용자가 리뷰를 작성하려고 할 때 발생하는 예외 HTTP 상태 코드 403(Forbidden)을 반환 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class ContentNotPurchasedException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  /** 기본 메시지로 예외 생성 */
  public ContentNotPurchasedException() {
    super("해당 콘텐츠를 구매한 사용자만 리뷰를 작성할 수 있습니다.");
  }

  /**
   * 사용자 정의 메시지로 예외 생성
   *
   * @param message 예외 메시지
   */
  public ContentNotPurchasedException(String message) {
    super(message);
  }

  /**
   * 회원용 예외 메시지
   *
   * @param contentId 콘텐츠 ID
   */
  public static ContentNotPurchasedException forMember(Long contentId) {
    return new ContentNotPurchasedException(
        String.format("사용자가 콘텐츠(ID: %d)를 구매하지 않아 리뷰를 작성할 수 없습니다.", contentId));
  }

  /**
   * 비회원용 예외 메시지
   *
   * @param contentId 콘텐츠 ID
   */
  public static ContentNotPurchasedException forGuest(Long contentId) {
    return new ContentNotPurchasedException(
        String.format("비회원 사용자가 콘텐츠(ID: %d)를 구매하지 않아 리뷰를 작성할 수 없습니다.", contentId));
  }
}
