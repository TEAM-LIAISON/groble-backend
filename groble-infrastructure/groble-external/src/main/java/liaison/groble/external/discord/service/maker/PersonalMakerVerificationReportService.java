package liaison.groble.external.discord.service.maker;

import liaison.groble.external.discord.dto.PersonalMakerVerificationCreateReportDto;

public interface PersonalMakerVerificationReportService {
  void sendCreatePersonalMakerVerificationReport(
      PersonalMakerVerificationCreateReportDto personalMakerVerificationCreateReportDto);
}
