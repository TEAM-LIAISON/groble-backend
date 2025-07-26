package liaison.groble.application.payment.service;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.domain.payment.entity.Payment;
import liaison.groble.domain.payment.entity.PayplePayment;
import liaison.groble.domain.payment.repository.PaymentRepository;
import liaison.groble.domain.payment.repository.PayplePaymentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentReader {
  private final PayplePaymentRepository payplePaymentRepository;
  private final PaymentRepository paymentRepository;

  public PayplePayment getPayplePaymentById(Long payplePaymentId) {
    return payplePaymentRepository
        .findById(payplePaymentId)
        .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다: " + payplePaymentId));
  }

  public PayplePayment getPayplePaymentByOid(String payOid) {
    return payplePaymentRepository
        .findByPcdPayOid(payOid)
        .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다: " + payOid));
  }

  public Payment getPaymentByOrderId(Long orderId) {
    return paymentRepository
        .findByOrderId(orderId)
        .orElseThrow(() -> new IllegalArgumentException("주문 정보를 찾을 수 없습니다: " + orderId));
  }

  public Payment getPaymentById(Long paymentId) {
    return paymentRepository
        .findById(paymentId)
        .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다: " + paymentId));
  }
}
