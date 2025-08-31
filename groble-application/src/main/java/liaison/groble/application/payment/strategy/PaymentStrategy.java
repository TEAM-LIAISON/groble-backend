package liaison.groble.application.payment.strategy;

import liaison.groble.application.payment.dto.AppCardPayplePaymentDTO;
import liaison.groble.application.payment.dto.PaypleAuthResultDTO;
import liaison.groble.application.payment.dto.cancel.PaymentCancelResponse;

/**
 * 결제 전략 인터페이스
 *
 * <p>사용자 타입별 결제 처리 로직을 캡슐화합니다. Strategy 패턴을 통해 회원/비회원 로직을 완전히 분리하고 확장 가능하게 합니다.
 */
public interface PaymentStrategy {

  /**
   * 앱카드 결제를 처리합니다.
   *
   * @param authResult 페이플 인증 결과
   * @param userId 회원 ID (회원인 경우)
   * @param guestUserId 비회원 ID (비회원인 경우)
   * @return 결제 처리 결과
   */
  AppCardPayplePaymentDTO processAppCardPayment(
      PaypleAuthResultDTO authResult, Long userId, Long guestUserId);

  /**
   * 결제를 취소합니다.
   *
   * @param merchantUid 주문번호
   * @param reason 취소 사유
   * @param userId 회원 ID (회원인 경우)
   * @param guestUserId 비회원 ID (비회원인 경우)
   * @return 취소 처리 결과
   */
  PaymentCancelResponse cancelPayment(
      String merchantUid, String reason, Long userId, Long guestUserId);

  /**
   * 해당 전략이 주어진 사용자 타입을 지원하는지 확인합니다.
   *
   * @param userId 회원 ID (회원인 경우)
   * @param guestUserId 비회원 ID (비회원인 경우)
   * @return 지원 여부
   */
  boolean supports(Long userId, Long guestUserId);

  /**
   * 전략 타입을 반환합니다. (로깅 및 모니터링 용도)
   *
   * @return 전략 타입
   */
  String getStrategyType();
}
