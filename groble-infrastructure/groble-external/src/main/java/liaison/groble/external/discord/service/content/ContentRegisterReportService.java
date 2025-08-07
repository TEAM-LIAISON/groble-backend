package liaison.groble.external.discord.service.content;

import liaison.groble.external.discord.dto.content.ContentRegisterCreateReportDTO;

public interface ContentRegisterReportService {
  void sendCreateContentRegisterReport(
      ContentRegisterCreateReportDTO contentRegisterCreateReportDTO);
}
