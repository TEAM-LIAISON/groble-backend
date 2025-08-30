package liaison.groble.api.server.payment.processor;

import org.springframework.stereotype.Component;

import liaison.groble.application.payment.dto.AppCardPayplePaymentResponse;
import liaison.groble.application.payment.dto.PaypleAuthResultDTO;
import liaison.groble.application.payment.dto.cancel.PaymentCancelResponse;
import liaison.groble.application.payment.service.PayplePaymentFacade;
import liaison.groble.common.model.Accessor;

/**
 * 회원 결제 처리기
 *
 * <p>인증된 일반 회원의 결제 및 취소를 담당합니다.
 */
@Component
public class MemberPaymentProcessor extends AbstractPaymentProcessor {

  public MemberPaymentProcessor(PayplePaymentFacade payplePaymentFacade) {
    super(payplePaymentFacade);
  }

  @Override
  public boolean supports(Accessor accessor) {
    return accessor.isAuthenticated() && !accessor.isGuest();
  }

  @Override
  protected AppCardPayplePaymentResponse executePayment(
      Accessor accessor, PaypleAuthResultDTO authResult) {
    return payplePaymentFacade.processAppCardPayment(accessor.getUserId(), authResult);
  }

  @Override
  protected PaymentCancelResponse executeCancel(
      Accessor accessor, String merchantUid, String reason) {
    return payplePaymentFacade.cancelPayment(accessor.getUserId(), merchantUid, reason);
  }

  @Override
  protected String getUserIdentifier(Accessor accessor) {
    return accessor.getUserId().toString();
  }

  @Override
  protected String getUserType() {
    return "회원";
  }
}
