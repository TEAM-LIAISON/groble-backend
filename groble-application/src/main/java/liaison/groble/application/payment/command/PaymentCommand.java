package liaison.groble.application.payment.command;

/**
 * 결제 명령 인터페이스
 *
 * <p>Command 패턴을 통해 결제 관련 요청을 객체로 캡슐화합니다. 각 명령은 실행에 필요한 모든 정보를 포함하고 있으며, 실행 결과를 반환합니다.
 *
 * @param <T> 명령 실행 결과 타입
 */
public interface PaymentCommand<T> {

  /**
   * 명령을 실행합니다.
   *
   * @return 실행 결과
   * @throws RuntimeException 실행 중 발생한 예외
   */
  T execute();

  /**
   * 명령 타입을 반환합니다. (로깅 및 모니터링 용도)
   *
   * @return 명령 타입
   */
  String getCommandType();

  /**
   * 명령 식별자를 반환합니다. (추적 용도)
   *
   * @return 명령 식별자 (예: merchantUid, orderId 등)
   */
  String getCommandId();
}
