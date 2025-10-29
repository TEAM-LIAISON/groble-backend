package liaison.groble.application.payment.service;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.springframework.stereotype.Service;

import liaison.groble.application.payment.dto.AppCardPayplePaymentDTO;
import liaison.groble.application.payment.dto.PaymentAuthInfo;
import liaison.groble.application.payment.dto.PaypleApprovalResult;
import liaison.groble.application.payment.dto.PaypleAuthResultDTO;
import liaison.groble.application.payment.dto.completion.PaymentCompletionResult;
import liaison.groble.application.payment.exception.PaymentAuthException;
import liaison.groble.application.payment.exception.PaymentProcessingException;
import liaison.groble.application.payment.exception.PayplePaymentApprovalException;
import liaison.groble.application.payment.util.PaypleErrorMessageResolver;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 결제 실행 서비스
 *
 * <p>결제 처리의 공통 플로우를 담당하는 서비스입니다. Template Method 패턴을 통해 일관된 결제 처리 흐름을 제공합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentExecutionService {

  private final PaypleApiClient paypleApiClient;
  private final PaymentTransactionService transactionService;

  /**
   * 결제를 실행합니다.
   *
   * @param authResult 페이플 인증 결과
   * @param authInfoSupplier 인증 정보 저장 및 검증 로직
   * @param completionFunction 결제 완료 처리 로직
   * @param eventPublisher 이벤트 발행 로직
   * @return 결제 처리 결과
   */
  public AppCardPayplePaymentDTO executePayment(
      PaypleAuthResultDTO authResult,
      Supplier<PaymentAuthInfo> authInfoSupplier,
      BiFunction<PaymentAuthInfo, PaypleApprovalResult, PaymentCompletionResult> completionFunction,
      Consumer<PaymentCompletionResult> eventPublisher) {

    try {
      // 1. 요청 검증
      validateAuthRequest(authResult);

      // 2. 결제창이 닫힌 경우 빈 응답 반환
      if (authResult.isClosed()) {
        log.info("결제창 닫힘 - merchantUid: {}", authResult.getPayOid());
        return AppCardPayplePaymentDTO.builder().build();
      }

      // 3. 인증 정보 저장 및 검증
      PaymentAuthInfo authInfo = authInfoSupplier.get();

      // 4. 페이플 승인 API 호출
      PaypleApprovalResult approvalResult = paypleApiClient.requestApproval(authResult);

      if (!approvalResult.isSuccess()) {
        // 5-1. 승인 실패 처리
        handleApprovalFailure(authInfo, approvalResult);
        String userMessage =
            PaypleErrorMessageResolver.resolveApprovalFailureMessage(approvalResult);
        throw new PayplePaymentApprovalException(userMessage);
      }

      // 5-2. 결제 완료 처리
      PaymentCompletionResult completionResult = completionFunction.apply(authInfo, approvalResult);

      // 6. 이벤트 발행
      eventPublisher.accept(completionResult);

      // 7. 응답 생성
      return buildPaymentResponse(approvalResult);

    } catch (PaymentAuthException e) {
      log.warn("결제 인증 실패 - merchantUid: {}, error: {}", authResult.getPayOid(), e.getMessage());
      throw e;
    } catch (PayplePaymentApprovalException e) {
      log.warn("결제 승인 실패 - merchantUid: {}, guidance: {}", authResult.getPayOid(), e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("결제 처리 중 예상치 못한 오류 - merchantUid: {}", authResult.getPayOid(), e);
      throw new PaymentProcessingException("결제 처리 중 오류가 발생했습니다", e);
    }
  }

  private void validateAuthRequest(PaypleAuthResultDTO authResult) {
    if (authResult.isError()) {
      throw new PaymentAuthException("페이플 인증 실패: " + authResult.getPayMsg());
    }
  }

  private void handleApprovalFailure(
      PaymentAuthInfo authInfo, PaypleApprovalResult approvalResult) {
    transactionService.handleApprovalFailure(
        authInfo.getOrderId(), approvalResult.getErrorCode(), approvalResult.getErrorMessage());
  }

  private AppCardPayplePaymentDTO buildPaymentResponse(PaypleApprovalResult result) {
    return AppCardPayplePaymentDTO.builder()
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
}
