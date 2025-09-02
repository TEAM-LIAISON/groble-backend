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
 * 회원 결제 처리기
 *
 * <p>인증된 일반 회원의 결제 및 취소를 담당합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MemberPaymentProcessor implements PaymentProcessor {

  private final PayplePaymentFacadeV2 payplePaymentFacadeV2;

  @Override
  public String getSupportedUserType() {
    return "MEMBER";
  }

  @Override
  public AppCardPayplePaymentDTO processPayment(
      UserContext userContext, PaypleAuthResultDTO authResult) {
    log.debug("회원 결제 처리 시작 - userId: {}", userContext.getId());
    return payplePaymentFacadeV2.processAppCardPayment(userContext.getId(), authResult);
  }

  @Override
  public PaymentCancelResponse cancelPayment(
      UserContext userContext, String merchantUid, String reason) {
    log.debug("회원 결제 취소 시작 - userId: {}, merchantUid: {}", userContext.getId(), merchantUid);
    return payplePaymentFacadeV2.cancelPayment(userContext.getId(), merchantUid, reason);
  }
}
