package liaison.groble.application.purchase.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 구매 목록 조회 시 적절한 인증이 필요할 때 발생하는 예외 회원 로그인 또는 비회원 전화번호 인증이 필요한 상황에서 사용 HTTP 상태 코드 401(Unauthorized)을
 * 반환
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class PurchaseAuthenticationRequiredException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  /** 기본 메시지로 예외 생성 */
  public PurchaseAuthenticationRequiredException() {
    super("구매 목록 조회를 위해서는 회원 로그인 또는 비회원 전화번호 인증이 필요합니다.");
  }

  /**
   * 사용자 정의 메시지로 예외 생성
   *
   * @param message 예외 메시지
   */
  public PurchaseAuthenticationRequiredException(String message) {
    super(message);
  }

  /** 구매 목록 조회용 예외 메시지 */
  public static PurchaseAuthenticationRequiredException forPurchaseList() {
    return new PurchaseAuthenticationRequiredException(
        "구매 목록 조회를 위해서는 회원 로그인 또는 비회원 전화번호 인증이 필요합니다.");
  }
}
