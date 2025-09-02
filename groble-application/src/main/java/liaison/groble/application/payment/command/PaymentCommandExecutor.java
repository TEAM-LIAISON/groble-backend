package liaison.groble.application.payment.command;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import liaison.groble.application.payment.exception.PaymentAuthenticationRequiredException;
import liaison.groble.application.payment.strategy.PaymentStrategy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 결제 명령 실행자
 *
 * <p>Command 패턴의 Invoker 역할을 담당합니다. 명령 실행 전후의 공통 처리(로깅, 메트릭 등)를 수행하고, 적절한 PaymentStrategy를 선택하여 명령을
 * 실행합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentCommandExecutor {

  private final List<PaymentStrategy> paymentStrategies;

  /**
   * 결제 명령을 실행합니다.
   *
   * @param command 실행할 명령
   * @param <T> 명령 실행 결과 타입
   * @return 실행 결과
   */
  public <T> T execute(PaymentCommand<T> command) {
    StopWatch stopWatch = new StopWatch();
    stopWatch.start();

    try {
      logCommandStart(command);

      T result = command.execute();

      stopWatch.stop();
      logCommandSuccess(command, stopWatch.getTotalTimeMillis());

      return result;

    } catch (Exception e) {
      stopWatch.stop();
      logCommandError(command, e, stopWatch.getTotalTimeMillis());
      throw e;
    }
  }

  /**
   * 앱카드 결제 명령을 생성합니다.
   *
   * @param authResult 페이플 인증 결과
   * @param userId 회원 ID (회원인 경우)
   * @param guestUserId 비회원 ID (비회원인 경우)
   * @return 앱카드 결제 명령
   */
  public AppCardPaymentCommand createAppCardPaymentCommand(
      liaison.groble.application.payment.dto.PaypleAuthResultDTO authResult,
      Long userId,
      Long guestUserId) {

    PaymentStrategy strategy = getPaymentStrategy(userId, guestUserId);

    return AppCardPaymentCommand.builder()
        .paymentStrategy(strategy)
        .authResult(authResult)
        .userId(userId)
        .guestUserId(guestUserId)
        .build();
  }

  /**
   * 결제 취소 명령을 생성합니다.
   *
   * @param merchantUid 주문번호
   * @param reason 취소 사유
   * @param userId 회원 ID (회원인 경우)
   * @param guestUserId 비회원 ID (비회원인 경우)
   * @return 결제 취소 명령
   */
  public PaymentCancelCommand createPaymentCancelCommand(
      String merchantUid, String reason, Long userId, Long guestUserId) {

    PaymentStrategy strategy = getPaymentStrategy(userId, guestUserId);

    return PaymentCancelCommand.builder()
        .paymentStrategy(strategy)
        .merchantUid(merchantUid)
        .reason(reason)
        .userId(userId)
        .guestUserId(guestUserId)
        .build();
  }

  private PaymentStrategy getPaymentStrategy(Long userId, Long guestUserId) {
    return paymentStrategies.stream()
        .filter(strategy -> strategy.supports(userId, guestUserId))
        .findFirst()
        .orElseThrow(
            () -> {
              log.warn("지원되지 않는 사용자 타입 - userId: {}, guestUserId: {}", userId, guestUserId);
              return PaymentAuthenticationRequiredException.forPayment();
            });
  }

  private void logCommandStart(PaymentCommand<?> command) {
    log.info("결제 명령 실행 시작 - type: {}, id: {}", command.getCommandType(), command.getCommandId());
  }

  private void logCommandSuccess(PaymentCommand<?> command, long executionTime) {
    log.info(
        "결제 명령 실행 완료 - type: {}, id: {}, 실행시간: {}ms",
        command.getCommandType(),
        command.getCommandId(),
        executionTime);
  }

  private void logCommandError(PaymentCommand<?> command, Exception e, long executionTime) {
    log.error(
        "결제 명령 실행 실패 - type: {}, id: {}, 실행시간: {}ms, 오류: {}",
        command.getCommandType(),
        command.getCommandId(),
        executionTime,
        e.getMessage(),
        e);
  }
}
