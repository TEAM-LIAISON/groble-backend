package liaison.groble.application.payment.command;

import liaison.groble.application.payment.dto.cancel.PaymentCancelResponse;
import liaison.groble.application.payment.strategy.PaymentStrategy;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 결제 취소 명령
 *
 * <p>결제 취소 요청을 캡슐화하는 Command 객체입니다. 사용자 타입(회원/비회원)에 관계없이 동일한 인터페이스로 처리됩니다.
 */
@Getter
@Builder
@RequiredArgsConstructor
public class PaymentCancelCommand implements PaymentCommand<PaymentCancelResponse> {

  private final PaymentStrategy paymentStrategy;
  private final String merchantUid;
  private final String reason;
  private final Long userId;
  private final Long guestUserId;

  @Override
  public PaymentCancelResponse execute() {
    return paymentStrategy.cancelPayment(merchantUid, reason, userId, guestUserId);
  }

  @Override
  public String getCommandType() {
    return "PAYMENT_CANCEL";
  }

  @Override
  public String getCommandId() {
    return merchantUid;
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
