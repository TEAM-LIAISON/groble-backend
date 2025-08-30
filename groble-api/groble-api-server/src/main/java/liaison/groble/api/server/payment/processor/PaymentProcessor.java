package liaison.groble.api.server.payment.processor;

import liaison.groble.application.payment.dto.AppCardPayplePaymentResponse;
import liaison.groble.application.payment.dto.PaypleAuthResultDTO;
import liaison.groble.application.payment.dto.cancel.PaymentCancelResponse;
import liaison.groble.common.model.Accessor;

/**
 * 결제 처리 전략 인터페이스
 *
 * <p>회원/비회원 등 다양한 사용자 타입에 대한 결제 처리를 추상화합니다. Strategy 패턴을 통해 각 사용자 타입별 결제 로직을 분리하고 확장 가능하게 합니다.
 */
public interface PaymentProcessor {

  /**
   * 앱카드 결제를 처리합니다.
   *
   * @param accessor 사용자 정보
   * @param authResult 페이플 인증 결과
   * @return 결제 처리 결과
   */
  AppCardPayplePaymentResponse processPayment(Accessor accessor, PaypleAuthResultDTO authResult);

  /**
   * 결제를 취소합니다.
   *
   * @param accessor 사용자 정보
   * @param merchantUid 주문번호
   * @param reason 취소 사유
   * @return 취소 처리 결과
   */
  PaymentCancelResponse cancelPayment(Accessor accessor, String merchantUid, String reason);

  /**
   * 해당 Processor가 주어진 사용자 타입을 지원하는지 확인합니다.
   *
   * @param accessor 사용자 정보
   * @return 지원 여부
   */
  boolean supports(Accessor accessor);
}
