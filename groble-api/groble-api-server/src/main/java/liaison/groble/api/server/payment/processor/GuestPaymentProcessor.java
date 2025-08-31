package liaison.groble.api.server.payment.processor;

import org.springframework.stereotype.Component;

import liaison.groble.application.payment.dto.AppCardPayplePaymentDTO;
import liaison.groble.application.payment.dto.PaypleAuthResultDTO;
import liaison.groble.application.payment.dto.cancel.PaymentCancelResponse;
import liaison.groble.application.payment.service.PayplePaymentFacadeV2;
import liaison.groble.common.context.UserContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 비회원 결제 처리기
 *
 * <p>비회원(게스트) 사용자의 결제 및 취소를 담당합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GuestPaymentProcessor implements PaymentProcessor {

  private final PayplePaymentFacadeV2 payplePaymentFacadeV2;

  @Override
  public String getSupportedUserType() {
    return "GUEST";
  }

  @Override
  public AppCardPayplePaymentDTO processPayment(
      UserContext userContext, PaypleAuthResultDTO authResult) {
    log.debug("비회원 결제 처리 시작 - guestUserId: {}", userContext.getId());
    return payplePaymentFacadeV2.processAppCardPaymentForGuest(userContext.getId(), authResult);
  }

  @Override
  public PaymentCancelResponse cancelPayment(
      UserContext userContext, String merchantUid, String reason) {
    log.debug("비회원 결제 취소 시작 - guestUserId: {}, merchantUid: {}", userContext.getId(), merchantUid);
    return payplePaymentFacadeV2.cancelPaymentForGuest(userContext.getId(), merchantUid, reason);
  }
}
