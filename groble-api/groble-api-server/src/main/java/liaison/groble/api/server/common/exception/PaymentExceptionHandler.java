package liaison.groble.api.server.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import liaison.groble.application.payment.exception.PaymentException;
import liaison.groble.common.response.GrobleResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * 결제 도메인 전용 예외 처리기.
 *
 * <p>Payple 등 외부 PG에서 내려온 에러 메시지를 사용자에게 전달할 수 있도록 HTTP 400 응답 형태로 변환한다.
 */
@Slf4j
@RestControllerAdvice
public class PaymentExceptionHandler {

  @ExceptionHandler(PaymentException.class)
  public ResponseEntity<GrobleResponse<Void>> handlePaymentException(PaymentException ex) {
    log.warn("결제 예외 발생 - message: {}", ex.getMessage(), ex);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(GrobleResponse.error(ex.getMessage(), HttpStatus.BAD_REQUEST.value()));
  }
}
