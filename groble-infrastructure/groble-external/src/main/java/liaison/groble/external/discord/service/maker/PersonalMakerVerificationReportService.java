package liaison.groble.external.discord.service.maker;

import liaison.groble.external.discord.dto.PersonalMakerVerificationCreateReportDTO;

public interface PersonalMakerVerificationReportService {
  void sendCreatePersonalMakerVerificationReport(
      PersonalMakerVerificationCreateReportDTO personalMakerVerificationCreateReportDTO);
}
