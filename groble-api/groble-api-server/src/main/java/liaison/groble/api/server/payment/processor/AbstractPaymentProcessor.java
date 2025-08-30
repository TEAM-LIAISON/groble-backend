package liaison.groble.api.server.payment.processor;

import org.springframework.util.StopWatch;

import liaison.groble.application.payment.dto.AppCardPayplePaymentResponse;
import liaison.groble.application.payment.dto.PaypleAuthResultDTO;
import liaison.groble.application.payment.dto.cancel.PaymentCancelResponse;
import liaison.groble.application.payment.service.PayplePaymentFacadeV2;
import liaison.groble.common.model.Accessor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 결제 처리기 추상 클래스
 *
 * <p>공통 로직과 템플릿 메서드를 제공하여 코드 중복을 방지하고 일관성을 보장합니다. OTEL 메트릭 수집과 성능 모니터링을 위한 기본 구조를 제공합니다.
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractPaymentProcessor implements PaymentProcessor {

  protected final PayplePaymentFacadeV2 payplePaymentFacadeV2;

  @Override
  public final AppCardPayplePaymentResponse processPayment(
      Accessor accessor, PaypleAuthResultDTO authResult) {
    StopWatch stopWatch = new StopWatch();
    stopWatch.start();

    try {
      validatePaymentRequest(accessor, authResult);
      logPaymentStart(accessor, authResult);

      AppCardPayplePaymentResponse response = executePayment(accessor, authResult);

      stopWatch.stop();
      logPaymentSuccess(accessor, authResult, stopWatch.getTotalTimeMillis());

      return response;

    } catch (Exception e) {
      stopWatch.stop();
      logPaymentError(accessor, authResult, e, stopWatch.getTotalTimeMillis());
      throw e;
    }
  }

  @Override
  public final PaymentCancelResponse cancelPayment(
      Accessor accessor, String merchantUid, String reason) {
    StopWatch stopWatch = new StopWatch();
    stopWatch.start();

    try {
      validateCancelRequest(accessor, merchantUid, reason);
      logCancelStart(accessor, merchantUid, reason);

      PaymentCancelResponse response = executeCancel(accessor, merchantUid, reason);

      stopWatch.stop();
      logCancelSuccess(accessor, merchantUid, stopWatch.getTotalTimeMillis());

      return response;

    } catch (Exception e) {
      stopWatch.stop();
      logCancelError(accessor, merchantUid, e, stopWatch.getTotalTimeMillis());
      throw e;
    }
  }

  // === 템플릿 메서드 (하위 클래스에서 구현) ===
  protected abstract AppCardPayplePaymentResponse executePayment(
      Accessor accessor, PaypleAuthResultDTO authResult);

  protected abstract PaymentCancelResponse executeCancel(
      Accessor accessor, String merchantUid, String reason);

  protected abstract String getUserIdentifier(Accessor accessor);

  protected abstract String getUserType();

  // === 검증 로직 (필요시 오버라이드) ===
  protected void validatePaymentRequest(Accessor accessor, PaypleAuthResultDTO authResult) {
    // 기본 검증 로직 (필요시 하위 클래스에서 확장)
  }

  protected void validateCancelRequest(Accessor accessor, String merchantUid, String reason) {
    // 기본 검증 로직 (필요시 하위 클래스에서 확장)
  }

  // === 로깅 메서드 ===
  private void logPaymentStart(Accessor accessor, PaypleAuthResultDTO authResult) {
    log.info(
        "{} 앱카드 결제 요청 시작 - {}: {}, merchantUid: {}",
        getUserType(),
        getUserType().toLowerCase() + "Id",
        getUserIdentifier(accessor),
        authResult.getPayOid());
  }

  private void logPaymentSuccess(
      Accessor accessor, PaypleAuthResultDTO authResult, long executionTime) {
    log.info(
        "{} 앱카드 결제 요청 완료 - {}: {}, merchantUid: {}, 실행시간: {}ms",
        getUserType(),
        getUserType().toLowerCase() + "Id",
        getUserIdentifier(accessor),
        authResult.getPayOid(),
        executionTime);
  }

  private void logPaymentError(
      Accessor accessor, PaypleAuthResultDTO authResult, Exception e, long executionTime) {
    log.error(
        "{} 앱카드 결제 요청 실패 - {}: {}, merchantUid: {}, 실행시간: {}ms, 오류: {}",
        getUserType(),
        getUserType().toLowerCase() + "Id",
        getUserIdentifier(accessor),
        authResult.getPayOid(),
        executionTime,
        e.getMessage(),
        e);
  }

  private void logCancelStart(Accessor accessor, String merchantUid, String reason) {
    log.info(
        "{} 결제 취소 요청 시작 - {}: {}, merchantUid: {}, reason: {}",
        getUserType(),
        getUserType().toLowerCase() + "Id",
        getUserIdentifier(accessor),
        merchantUid,
        reason);
  }

  private void logCancelSuccess(Accessor accessor, String merchantUid, long executionTime) {
    log.info(
        "{} 결제 취소 요청 완료 - {}: {}, merchantUid: {}, 실행시간: {}ms",
        getUserType(),
        getUserType().toLowerCase() + "Id",
        getUserIdentifier(accessor),
        merchantUid,
        executionTime);
  }

  private void logCancelError(
      Accessor accessor, String merchantUid, Exception e, long executionTime) {
    log.error(
        "{} 결제 취소 요청 실패 - {}: {}, merchantUid: {}, 실행시간: {}ms, 오류: {}",
        getUserType(),
        getUserType().toLowerCase() + "Id",
        getUserIdentifier(accessor),
        merchantUid,
        executionTime,
        e.getMessage(),
        e);
  }
}
