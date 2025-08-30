package liaison.groble.application.payment.command;

import liaison.groble.application.payment.dto.AppCardPayplePaymentResponse;
import liaison.groble.application.payment.dto.PaypleAuthResultDTO;
import liaison.groble.application.payment.strategy.PaymentStrategy;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 앱카드 결제 명령
 *
 * <p>앱카드 결제 요청을 캡슐화하는 Command 객체입니다. 사용자 타입(회원/비회원)에 관계없이 동일한 인터페이스로 처리됩니다.
 */
@Getter
@Builder
@RequiredArgsConstructor
public class AppCardPaymentCommand implements PaymentCommand<AppCardPayplePaymentResponse> {

  private final PaymentStrategy paymentStrategy;
  private final PaypleAuthResultDTO authResult;
  private final Long userId;
  private final Long guestUserId;

  @Override
  public AppCardPayplePaymentResponse execute() {
    return paymentStrategy.processAppCardPayment(authResult, userId, guestUserId);
  }

  @Override
  public String getCommandType() {
    return "APP_CARD_PAYMENT";
  }

  @Override
  public String getCommandId() {
    return authResult.getPayOid();
  }

  /**
   * 사용자 식별자를 반환합니다.
   *
   * @return 회원은 userId, 비회원은 guestUserId
   */
  public String getUserIdentifier() {
    return userId != null ? "userId:" + userId : "guestUserId:" + guestUserId;
  }

  /**
   * 사용자 타입을 반환합니다.
   *
   * @return 회원/비회원 구분
   */
  public String getUserType() {
    return userId != null ? "회원" : "비회원";
  }
}
