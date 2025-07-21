package liaison.groble.external.discord.service.maker;

import liaison.groble.external.discord.dto.BusinessMakerVerificationCreateReportDTO;

public interface BusinessMakerVerificationReportService {
  void sendCreateBusinessMakerVerificationReport(
      BusinessMakerVerificationCreateReportDTO businessMakerVerificationCreateReportDTO);
}
