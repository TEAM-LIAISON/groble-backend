package liaison.groble.external.discord.service.content;

import liaison.groble.external.discord.dto.ContentRegisterCreateReportDTO;

public interface ContentRegisterReportService {
  void sendCreateContentRegisterReport(
      ContentRegisterCreateReportDTO contentRegisterCreateReportDto);
}
