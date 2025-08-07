package liaison.groble.external.discord.service.payment;

import liaison.groble.external.discord.dto.payment.ContentPaymentRefundReportDTO;

public interface ContentPaymentRefundRequestService {
  void sendContentPaymentRefundRequestReport(
      ContentPaymentRefundReportDTO contentPaymentRefundReportDTO);
}
