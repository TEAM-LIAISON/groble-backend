package liaison.groble.application.payment.service;

import java.util.Map;

import liaison.groble.application.payment.dto.PaymentApproveDto;
import liaison.groble.application.payment.dto.PaymentCancelDto;
import liaison.groble.application.payment.dto.PaymentPrepareDto;
import liaison.groble.application.payment.dto.PaymentResultDto;
import liaison.groble.application.payment.dto.VirtualAccountDto;

public interface PortOnePaymentService {

  // 결제 준비 (키 발급)
  PaymentResultDto preparePayment(PaymentPrepareDto prepareDto);

  // 결제 승인
  PaymentResultDto approvePayment(PaymentApproveDto approveDto);

  // 결제 취소
  PaymentResultDto cancelPayment(PaymentCancelDto cancelDto);

  // 결제 정보 조회
  PaymentResultDto getPaymentDetails(String paymentKey);

  // 가상계좌 발급
  PaymentResultDto issueVirtualAccount(VirtualAccountDto virtualAccountDto);

  // 웹훅 처리
  void handleWebhook(Map<String, Object> webhookData);

  String getWebhookSecret();

  Map<String, String> getClientKey();
}
