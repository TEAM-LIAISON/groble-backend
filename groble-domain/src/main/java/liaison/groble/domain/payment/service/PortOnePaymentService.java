package liaison.groble.domain.payment.service;

import java.math.BigDecimal;
import java.util.Map;

import liaison.groble.domain.order.entity.Order;
import liaison.groble.domain.payment.entity.Payment;
import liaison.groble.domain.payment.entity.PaymentCancel;

public interface PortOnePaymentService {

  // 결제 준비 (키 발급)
  Payment preparePayment(
      Order order, Payment.PaymentMethod method, Map<String, Object> additionalData);

  // 결제 승인
  Payment approvePayment(String paymentKey, String orderId, BigDecimal amount);

  // 결제 취소
  PaymentCancel cancelPayment(String paymentKey, BigDecimal amount, String reason);

  // 결제 정보 조회
  Payment getPaymentDetails(String paymentKey);

  // 가상계좌 발급
  Payment issueVirtualAccount(Order order, Map<String, Object> bankInfo);

  // 웹훅 처리
  void handleWebhook(Map<String, Object> webhookData);
}
