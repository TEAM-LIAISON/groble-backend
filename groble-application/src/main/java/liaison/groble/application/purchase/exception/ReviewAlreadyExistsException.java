package liaison.groble.application.purchase.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** 이미 리뷰가 존재하는 콘텐츠에 중복으로 리뷰를 작성하려고 할 때 발생하는 예외 HTTP 상태 코드 409(Conflict)을 반환 */
@ResponseStatus(HttpStatus.CONFLICT)
public class ReviewAlreadyExistsException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  /** 기본 메시지로 예외 생성 */
  public ReviewAlreadyExistsException() {
    super("이미 해당 콘텐츠에 대한 리뷰가 존재합니다.");
  }

  /**
   * 사용자 정의 메시지로 예외 생성
   *
   * @param message 예외 메시지
   */
  public ReviewAlreadyExistsException(String message) {
    super(message);
  }

  /**
   * 회원용 예외 메시지
   *
   * @param contentId 콘텐츠 ID
   */
  public static ReviewAlreadyExistsException forMember(Long contentId) {
    return new ReviewAlreadyExistsException(
        String.format("이미 콘텐츠(ID: %d)에 대한 리뷰가 존재합니다. 중복 리뷰는 작성할 수 없습니다.", contentId));
  }

  /**
   * 비회원용 예외 메시지
   *
   * @param contentId 콘텐츠 ID
   */
  public static ReviewAlreadyExistsException forGuest(Long contentId) {
    return new ReviewAlreadyExistsException(
        String.format("이미 콘텐츠(ID: %d)에 대한 비회원 리뷰가 존재합니다. 중복 리뷰는 작성할 수 없습니다.", contentId));
  }
}
