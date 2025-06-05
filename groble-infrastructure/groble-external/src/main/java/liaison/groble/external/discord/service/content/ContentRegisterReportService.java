package liaison.groble.external.discord.service.content;

import liaison.groble.external.discord.dto.ContentRegisterCreateReportDto;

public interface ContentRegisterReportService {
  void sendCreateContentRegisterReport(
      ContentRegisterCreateReportDto contentRegisterCreateReportDto);
}
