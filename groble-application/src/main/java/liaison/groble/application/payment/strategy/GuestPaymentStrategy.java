package liaison.groble.application.payment.strategy;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import liaison.groble.application.payment.dto.AppCardPayplePaymentDTO;
import liaison.groble.application.payment.dto.PaymentCancelInfo;
import liaison.groble.application.payment.dto.PaymentCancelResult;
import liaison.groble.application.payment.dto.PaypleAuthResultDTO;
import liaison.groble.application.payment.dto.PaypleRefundResult;
import liaison.groble.application.payment.dto.cancel.PaymentCancelResponse;
import liaison.groble.application.payment.event.PaymentEventPublisher;
import liaison.groble.application.payment.exception.PaypleRefundException;
import liaison.groble.application.payment.service.PaymentExecutionService;
import liaison.groble.application.payment.service.PaymentTransactionService;
import liaison.groble.application.payment.service.PaypleApiClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 비회원 결제 전략
 *
 * <p>비회원(게스트) 사용자의 결제 및 취소 로직을 담당합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GuestPaymentStrategy implements PaymentStrategy {

  private final PaypleApiClient paypleApiClient;
  private final PaymentTransactionService transactionService;
  private final PaymentEventPublisher eventPublisher;
  private final PaymentExecutionService executionService;

  @Override
  public boolean supports(Long userId, Long guestUserId) {
    return userId == null && guestUserId != null;
  }

  @Override
  public String getStrategyType() {
    return "GUEST";
  }

  @Override
  public AppCardPayplePaymentDTO processAppCardPayment(
      PaypleAuthResultDTO authResult, Long userId, Long guestUserId) {

    log.info(
        "비회원 앱카드 결제 처리 시작 - guestUserId: {}, merchantUid: {}", guestUserId, authResult.getPayOid());

    return executionService.executePayment(
        authResult,
        () -> transactionService.saveAuthAndValidateForGuest(guestUserId, authResult),
        transactionService::completePaymentForGuest,
        eventPublisher::publishPaymentCompletedForGuest);
  }

  @Override
  public PaymentCancelResponse cancelPayment(
      String merchantUid, String reason, Long userId, Long guestUserId) {

    log.info("비회원 결제 취소 처리 시작 - guestUserId: {}, merchantUid: {}", guestUserId, merchantUid);

    try {
      // 1. 취소 가능 여부 검증
      PaymentCancelInfo cancelInfo =
          transactionService.validateCancellationForGuest(guestUserId, merchantUid);

      // 2. 페이플 환불 API 호출 (개선된 전용 인증 사용)
      PaypleRefundResult refundResult = paypleApiClient.requestRefund(cancelInfo);
      if (!refundResult.isSuccess()) {
        throw new PaypleRefundException(
            String.format(
                "비회원 환불 실패 [%s]: %s", refundResult.getErrorCode(), refundResult.getErrorMessage()));
      }

      // 3. 취소 완료 처리
      PaymentCancelResult cancelResult =
          transactionService.completeCancelForGuest(cancelInfo, reason);

      // 4. 환불 완료 이벤트 발행
      eventPublisher.publishPaymentRefundedForGuest(cancelResult);

      // 5. 응답 생성
      return buildCancelResponse(merchantUid, reason, cancelResult);

    } catch (PaypleRefundException e) {
      log.error(
          "비회원 결제 취소 실패 - guestUserId: {}, merchantUid: {}, reason: {}",
          guestUserId,
          merchantUid,
          e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error(
          "비회원 결제 취소 중 예상치 못한 오류 발생 - guestUserId: {}, merchantUid: {}",
          guestUserId,
          merchantUid,
          e);
      throw new PaypleRefundException("결제 취소 처리 중 오류가 발생했습니다", e);
    }
  }

  private PaymentCancelResponse buildCancelResponse(
      String merchantUid, String reason, PaymentCancelResult cancelResult) {
    return PaymentCancelResponse.builder()
        .merchantUid(merchantUid)
        .status("CANCELLED")
        .canceledAt(LocalDateTime.now())
        .cancelReason(reason)
        .refundAmount(cancelResult.getRefundAmount())
        .build();
  }
}
