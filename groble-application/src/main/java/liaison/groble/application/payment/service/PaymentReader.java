package liaison.groble.application.payment.service;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.domain.payment.entity.PayplePayment;
import liaison.groble.domain.payment.repository.PayplePaymentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentReader {
  private final PayplePaymentRepository payplePaymentRepository;

  public PayplePayment getPayplePaymentByOid(String payOid) {
    return payplePaymentRepository
        .findByPcdPayOid(payOid)
        .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다: " + payOid));
  }
}
