package liaison.groble.external.discord.service.payment;

import liaison.groble.external.discord.dto.payment.PaymentFailureReportDTO;

public interface PaymentFailureReportService {
  void sendPaymentFailureReport(PaymentFailureReportDTO paymentFailureReportDTO);
}
