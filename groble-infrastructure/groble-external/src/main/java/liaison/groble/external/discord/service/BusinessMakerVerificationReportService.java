package liaison.groble.external.discord.service;

import liaison.groble.external.discord.dto.BusinessMakerVerificationCreateReportDto;

public interface BusinessMakerVerificationReportService {
  void sendCreateBusinessMakerVerificationReport(
      BusinessMakerVerificationCreateReportDto businessMakerVerificationCreateReportDto);
}
