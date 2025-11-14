package liaison.groble.application.payment.event;

import org.springframework.stereotype.Component;

import liaison.groble.application.payment.dto.PaymentCancelResult;
import liaison.groble.application.payment.dto.completion.PaymentCompletionResult;
import liaison.groble.common.event.EventPublisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 결제 이벤트 발행자
 *
 * <p>결제 관련 이벤트 발행을 전담하는 컴포넌트입니다. 회원/비회원 구분에 따른 이벤트 생성 로직을 캡슐화합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventPublisher {

  private final EventPublisher eventPublisher;

  /**
   * 회원 결제 완료 이벤트를 발행합니다.
   *
   * @param completionResult 결제 완료 결과
   */
  public void publishPaymentCompleted(PaymentCompletionResult completionResult) {
    log.info("회원 결제 완료 이벤트 발행 시작 - orderId: {}", completionResult.getOrderId());

    try {
      PaymentCompletedEvent event =
          PaymentCompletedEvent.builder()
              .orderId(completionResult.getOrderId())
              .merchantUid(completionResult.getMerchantUid())
              .paymentId(completionResult.getPaymentId())
              .purchaseId(completionResult.getPurchaseId())
              .userId(completionResult.getUserId())
              .contentId(completionResult.getContentId())
              .sellerId(completionResult.getSellerId())
              .amount(completionResult.getAmount())
              .completedAt(completionResult.getCompletedAt())
              .sellerEmail(completionResult.getSellerEmail())
              .contentTitle(completionResult.getContentTitle())
              .nickname(completionResult.getNickname())
              .contentType(completionResult.getContentType())
              .paymentType(completionResult.getPaymentType())
              .optionId(completionResult.getOptionId())
              .selectedOptionName(completionResult.getSelectedOptionName())
              .purchasedAt(completionResult.getPurchasedAt())
              .subscriptionRenewal(completionResult.isSubscriptionRenewal())
              .subscriptionId(completionResult.getSubscriptionId())
              .subscriptionNextBillingDate(completionResult.getSubscriptionNextBillingDate())
              .subscriptionRound(completionResult.getSubscriptionRound())
              .build();

      eventPublisher.publish(event);
      log.info("회원 결제 완료 이벤트 발행 완료 - orderId: {}", completionResult.getOrderId());

    } catch (Exception e) {
      log.error("회원 결제 완료 이벤트 발행 실패 - orderId: {}", completionResult.getOrderId(), e);
      throw e;
    }
  }

  /**
   * 비회원 결제 완료 이벤트를 발행합니다.
   *
   * @param completionResult 결제 완료 결과
   */
  public void publishPaymentCompletedForGuest(PaymentCompletionResult completionResult) {
    log.info("비회원 결제 완료 이벤트 발행 시작 - orderId: {}", completionResult.getOrderId());

    try {
      PaymentCompletedEvent event =
          PaymentCompletedEvent.builder()
              .orderId(completionResult.getOrderId())
              .merchantUid(completionResult.getMerchantUid())
              .paymentId(completionResult.getPaymentId())
              .purchaseId(completionResult.getPurchaseId())
              .guestUserId(completionResult.getGuestUserId())
              .contentId(completionResult.getContentId())
              .sellerId(completionResult.getSellerId())
              .amount(completionResult.getAmount())
              .completedAt(completionResult.getCompletedAt())
              .sellerEmail(completionResult.getSellerEmail())
              .contentTitle(completionResult.getContentTitle())
              .guestUserName(completionResult.getGuestUserName())
              .contentType(completionResult.getContentType())
              .paymentType(completionResult.getPaymentType())
              .optionId(completionResult.getOptionId())
              .selectedOptionName(completionResult.getSelectedOptionName())
              .purchasedAt(completionResult.getPurchasedAt())
              .subscriptionRenewal(completionResult.isSubscriptionRenewal())
              .subscriptionId(completionResult.getSubscriptionId())
              .subscriptionNextBillingDate(completionResult.getSubscriptionNextBillingDate())
              .subscriptionRound(completionResult.getSubscriptionRound())
              .build();

      eventPublisher.publish(event);
      log.info("비회원 결제 완료 이벤트 발행 완료 - orderId: {}", completionResult.getOrderId());

    } catch (Exception e) {
      log.error("비회원 결제 완료 이벤트 발행 실패 - orderId: {}", completionResult.getOrderId(), e);
      throw e;
    }
  }

  /**
   * 회원 결제 환불 이벤트를 발행합니다.
   *
   * @param result 결제 취소 결과
   */
  public void publishPaymentRefunded(PaymentCancelResult result) {
    log.info("회원 결제 환불 이벤트 발행 - orderId: {}", result.getOrderId());

    PaymentRefundedEvent event =
        PaymentRefundedEvent.builder()
            .orderId(result.getOrderId())
            .paymentId(result.getPaymentId())
            .userId(result.getUserId())
            .refundAmount(result.getRefundAmount())
            .reason(result.getReason())
            .refundedAt(result.getRefundedAt())
            .build();

    eventPublisher.publish(event);
  }

  /**
   * 비회원 결제 환불 이벤트를 발행합니다.
   *
   * @param result 결제 취소 결과
   */
  public void publishPaymentRefundedForGuest(PaymentCancelResult result) {
    log.info("비회원 결제 환불 이벤트 발행 - orderId: {}", result.getOrderId());

    PaymentRefundedEvent event =
        PaymentRefundedEvent.builder()
            .orderId(result.getOrderId())
            .paymentId(result.getPaymentId())
            .guestUserId(result.getGuestUserId())
            .refundAmount(result.getRefundAmount())
            .reason(result.getReason())
            .refundedAt(result.getRefundedAt())
            .build();

    eventPublisher.publish(event);
  }
}
