package liaison.groble.external.discord.service.payment;

import liaison.groble.external.discord.dto.payment.ContentPaymentSuccessReportDTO;

public interface SubscriptionPaymentSuccessReportService {
  void sendSubscriptionPaymentSuccessReport(
      ContentPaymentSuccessReportDTO contentPaymentSuccessReportDTO);
}
