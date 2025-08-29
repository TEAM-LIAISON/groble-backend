package liaison.groble.application.order.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 주문 생성 또는 조회 시 적절한 인증이 필요할 때 발생하는 예외 회원 로그인 또는 비회원 전화번호 인증이 필요한 상황에서 사용 HTTP 상태 코드
 * 401(Unauthorized)을 반환
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class OrderAuthenticationRequiredException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  /** 기본 메시지로 예외 생성 */
  public OrderAuthenticationRequiredException() {
    super("주문을 위해서는 회원 로그인 또는 비회원 전화번호 인증이 필요합니다.");
  }

  /**
   * 사용자 정의 메시지로 예외 생성
   *
   * @param message 예외 메시지
   */
  public OrderAuthenticationRequiredException(String message) {
    super(message);
  }

  /** 주문 생성용 예외 메시지 */
  public static OrderAuthenticationRequiredException forOrderCreation() {
    return new OrderAuthenticationRequiredException("주문 생성을 위해서는 회원 로그인 또는 비회원 전화번호 인증이 필요합니다.");
  }

  /** 주문 조회용 예외 메시지 */
  public static OrderAuthenticationRequiredException forOrderInquiry() {
    return new OrderAuthenticationRequiredException("주문 조회를 위해서는 회원 로그인 또는 비회원 전화번호 인증이 필요합니다.");
  }
}
