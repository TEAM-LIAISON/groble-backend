package liaison.groble.api.server.payment.processor;

import org.springframework.stereotype.Component;

import liaison.groble.application.payment.dto.AppCardPayplePaymentResponse;
import liaison.groble.application.payment.dto.PaypleAuthResultDTO;
import liaison.groble.application.payment.dto.cancel.PaymentCancelResponse;
import liaison.groble.application.payment.service.PayplePaymentFacadeV2;
import liaison.groble.common.model.Accessor;

/**
 * 비회원 결제 처리기
 *
 * <p>비회원(게스트) 사용자의 결제 및 취소를 담당합니다.
 */
@Component
public class GuestPaymentProcessor extends AbstractPaymentProcessor {

  public GuestPaymentProcessor(PayplePaymentFacadeV2 payplePaymentFacadeV2) {
    super(payplePaymentFacadeV2);
  }

  @Override
  public boolean supports(Accessor accessor) {
    return accessor.isGuest();
  }

  @Override
  protected AppCardPayplePaymentResponse executePayment(
      Accessor accessor, PaypleAuthResultDTO authResult) {
    return payplePaymentFacadeV2.processAppCardPaymentForGuest(accessor.getId(), authResult);
  }

  @Override
  protected PaymentCancelResponse executeCancel(
      Accessor accessor, String merchantUid, String reason) {
    return payplePaymentFacadeV2.cancelPaymentForGuest(accessor.getId(), merchantUid, reason);
  }

  @Override
  protected String getUserIdentifier(Accessor accessor) {
    return accessor.getId().toString();
  }

  @Override
  protected String getUserType() {
    return "비회원";
  }
}
