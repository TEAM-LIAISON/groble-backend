package liaison.groble.application.payment.service;

import org.springframework.stereotype.Service;

import liaison.groble.application.payment.validator.PaymentValidator;

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
  private final PaypleApiClient paypleApiClient;
  private final PaymentValidator paymentValidator;
}
