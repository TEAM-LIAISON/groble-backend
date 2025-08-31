package liaison.groble.api.server.payment.processor;

import liaison.groble.application.payment.dto.AppCardPayplePaymentDTO;
import liaison.groble.application.payment.dto.PaypleAuthResultDTO;
import liaison.groble.application.payment.dto.cancel.PaymentCancelResponse;
import liaison.groble.common.context.UserContext;
import liaison.groble.common.strategy.UserTypeProcessor;

/**
 * 결제 처리 전략 인터페이스
 *
 * <p>회원/비회원 등 다양한 사용자 타입에 대한 결제 처리를 추상화합니다. Strategy 패턴을 통해 각 사용자 타입별 결제 로직을 분리하고 확장 가능하게 합니다.
 */
public interface PaymentProcessor extends UserTypeProcessor {

  /**
   * 앱카드 결제를 처리합니다.
   *
   * @param userContext 사용자 컨텍스트
   * @param authResult 페이플 인증 결과
   * @return 결제 처리 결과
   */
  AppCardPayplePaymentDTO processPayment(UserContext userContext, PaypleAuthResultDTO authResult);

  /**
   * 결제를 취소합니다.
   *
   * @param userContext 사용자 컨텍스트
   * @param merchantUid 주문번호
   * @param reason 취소 사유
   * @return 취소 처리 결과
   */
  PaymentCancelResponse cancelPayment(UserContext userContext, String merchantUid, String reason);
}
