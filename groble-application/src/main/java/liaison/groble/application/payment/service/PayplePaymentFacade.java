package liaison.groble.application.payment.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import liaison.groble.application.payment.dto.AppCardPayplePaymentResponse;
import liaison.groble.application.payment.dto.PaymentAuthInfo;
import liaison.groble.application.payment.dto.PaymentCancelInfo;
import liaison.groble.application.payment.dto.PaymentCancelResult;
import liaison.groble.application.payment.dto.PaymentCompletionResult;
import liaison.groble.application.payment.dto.PaypleApprovalResult;
import liaison.groble.application.payment.dto.PaypleAuthResultDTO;
import liaison.groble.application.payment.dto.PaypleRefundResult;
import liaison.groble.application.payment.dto.cancel.PaymentCancelResponse;
import liaison.groble.application.payment.event.PaymentCompletedEvent;
import liaison.groble.application.payment.event.PaymentRefundedEvent;
import liaison.groble.application.payment.exception.OrderNotFoundException;
import liaison.groble.application.payment.exception.PaymentAuthException;
import liaison.groble.application.payment.exception.PaymentNotFoundException;
import liaison.groble.application.payment.exception.PaymentProcessingException;
import liaison.groble.application.payment.exception.PaymentValidationException;
import liaison.groble.application.payment.exception.PaypleApiException;
import liaison.groble.application.payment.exception.PayplePaymentApprovalException;
import liaison.groble.application.payment.exception.PaypleRefundException;
import liaison.groble.application.payment.validator.PaymentValidator;
import liaison.groble.common.event.EventPublisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 페이플 결제 처리를 위한 Facade 서비스
 *
 * <p>복잡한 결제 프로세스를 조율하고 트랜잭션 경계를 관리합니다. 외부 API 호출과 DB 작업을 분리하여 트랜잭션 최적화를 수행합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PayplePaymentFacade {

  private final PaymentValidator paymentValidator;
  private final PaypleApiClient paypleApiClient;
  private final PaymentTransactionService transactionService;
  //    private final PaymentMapper paymentMapper;
  private final EventPublisher eventPublisher;

  /**
   * 앱카드 결제 처리
   *
   * <p>결제 인증 결과를 검증하고 승인 요청을 처리합니다. 외부 API 호출은 트랜잭션 외부에서 수행하고, DB 작업만 트랜잭션으로 처리합니다.
   *
   * @param userId 사용자 ID
   * @param paypleAuthResultDTO 인증 결과 요청
   * @return 결제 승인 응답
   */
  public AppCardPayplePaymentResponse processAppCardPayment(
      Long userId, PaypleAuthResultDTO paypleAuthResultDTO) {

    // 1. 요청 검증
    validateAuthRequest(paypleAuthResultDTO);

    // 2. 사용자가 결제창을 닫은 경우 빈 응답 반환
    if (paypleAuthResultDTO.isClosed()) {
      log.info("결제창 닫힘 - userId: {}, merchantUid: {}", userId, paypleAuthResultDTO.getPayOid());
      return AppCardPayplePaymentResponse.builder().build();
    }

    try {
      // 3. 트랜잭션 내에서 인증 정보 저장 및 검증
      PaymentAuthInfo authInfo =
          transactionService.saveAuthAndValidate(userId, paypleAuthResultDTO);

      // 4. 트랜잭션 외부에서 페이플 승인 API 호출
      PaypleApprovalResult approvalResult = paypleApiClient.requestApproval(paypleAuthResultDTO);

      if (!approvalResult.isSuccess()) {
        // 5-1. 승인 실패 시 주문 실패 처리
        transactionService.handleApprovalFailure(
            authInfo.getOrderId(), approvalResult.getErrorCode(), approvalResult.getErrorMessage());

        throw new PayplePaymentApprovalException("결제 승인 실패: " + approvalResult.getErrorMessage());
      }

      // 5-2. 승인 성공 시 결제 완료 처리
      PaymentCompletionResult completionResult =
          transactionService.completePayment(authInfo, approvalResult);

      // 6. 결제 완료 이벤트 발행
      publishPaymentCompletedEvent(completionResult);

      // 7. 응답 생성
      return buildPaymentResponse(approvalResult);

    } catch (PaymentAuthException | PaymentValidationException e) {
      log.warn(
          "결제 검증 실패 - userId: {}, merchantUid: {}, error: {}",
          userId,
          paypleAuthResultDTO.getPayOid(),
          e.getMessage());
      throw e;
    } catch (PaypleApiException e) {
      log.error("페이플 API 호출 실패 - merchantUid: {}", paypleAuthResultDTO.getPayOid(), e);
      throw new PayplePaymentApprovalException("결제 처리 중 오류가 발생했습니다", e);
    } catch (Exception e) {
      log.error("결제 처리 중 예상치 못한 오류 - merchantUid: {}", paypleAuthResultDTO.getPayOid(), e);
      throw new PaymentProcessingException("결제 처리 중 오류가 발생했습니다", e);
    }
  }

  /**
   * 결제 취소 처리
   *
   * @param userId 사용자 ID
   * @param merchantUid 주문번호
   * @param reason 취소 사유
   * @return 취소 응답
   */
  public PaymentCancelResponse cancelPayment(Long userId, String merchantUid, String reason) {
    log.info("결제 취소 처리 시작 - userId: {}, merchantUid: {}", userId, merchantUid);

    try {
      // 1. 트랜잭션 내에서 취소 가능 여부 검증
      PaymentCancelInfo cancelInfo = transactionService.validateCancellation(userId, merchantUid);

      // 2. 트랜잭션 외부에서 페이플 환불 API 호출
      PaypleRefundResult refundResult = paypleApiClient.requestRefund(cancelInfo);

      if (!refundResult.isSuccess()) {
        throw new PaypleRefundException(
            String.format(
                "환불 실패 [%s]: %s", refundResult.getErrorCode(), refundResult.getErrorMessage()));
      }

      // 3. 트랜잭션 내에서 취소 완료 처리
      PaymentCancelResult cancelResult = transactionService.completeCancel(cancelInfo, reason);

      // 4. 환불 완료 이벤트 발행
      publishPaymentRefundedEvent(cancelResult);

      // 5. 응답 생성
      return PaymentCancelResponse.builder()
          .merchantUid(merchantUid)
          .status("CANCELLED")
          .canceledAt(LocalDateTime.now())
          .cancelReason(reason)
          .refundAmount(cancelResult.getRefundAmount())
          .build();

    } catch (OrderNotFoundException | PaymentNotFoundException e) {
      log.warn("결제 취소 실패 - 정보 없음: merchantUid={}", merchantUid);
      throw e;
    } catch (PaymentValidationException e) {
      log.warn("결제 취소 검증 실패 - merchantUid={}, error={}", merchantUid, e.getMessage());
      throw e;
    } catch (PaypleApiException e) {
      log.error("페이플 환불 API 호출 실패 - merchantUid={}", merchantUid, e);
      throw new PaypleRefundException("환불 처리 중 오류가 발생했습니다", e);
    } catch (Exception e) {
      log.error("결제 취소 중 예상치 못한 오류 - merchantUid={}", merchantUid, e);
      throw new PaymentProcessingException("결제 취소 처리 중 오류가 발생했습니다", e);
    }
  }

  /** 인증 요청 검증 */
  private void validateAuthRequest(PaypleAuthResultDTO paypleAuthResultDTO) {
    if (paypleAuthResultDTO.isError()) {
      throw new PaymentAuthException("페이플 인증 실패: " + paypleAuthResultDTO.getPayMsg());
    }
  }

  /** 결제 응답 생성 */
  private AppCardPayplePaymentResponse buildPaymentResponse(PaypleApprovalResult result) {
    return AppCardPayplePaymentResponse.builder()
        .payRst(result.getPayRst())
        .payCode(result.getPayCode())
        .payMsg(result.getPayMsg())
        .payOid(result.getPayOid())
        .payType(result.getPayType())
        .payTime(result.getPayTime())
        .payTotal(result.getPayTotal())
        .payCardName(result.getPayCardName())
        .payCardNum(result.getPayCardNum())
        .payCardQuota(result.getPayCardQuota())
        .payCardTradeNum(result.getPayCardTradeNum())
        .payCardAuthNo(result.getPayCardAuthNo())
        .payCardReceipt(result.getPayCardReceipt())
        .build();
  }

  /** 결제 완료 이벤트 발행 */
  private void publishPaymentCompletedEvent(PaymentCompletionResult completionResult) {
    log.info("=== 이벤트 발행 시작 === orderId: {}", completionResult.getOrderId());

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
              .optionId(completionResult.getOptionId())
              .selectedOptionName(completionResult.getSelectedOptionName())
              .purchasedAt(completionResult.getPurchasedAt())
              .build();

      log.info("이벤트 객체 생성 완료: orderId={}", event.getOrderId());

      eventPublisher.publish(event); // 이 부분이 있나요?

      log.info("=== 이벤트 발행 완료 === orderId: {}", completionResult.getOrderId());

    } catch (Exception e) {
      log.error("이벤트 발행 중 예외 발생 - orderId: {}", completionResult.getOrderId(), e);
      throw e; // 또는 log만 남기고 넘어갈지 결정
    }
  }

  /** 환불 완료 이벤트 발행 */
  private void publishPaymentRefundedEvent(PaymentCancelResult result) {
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
}
