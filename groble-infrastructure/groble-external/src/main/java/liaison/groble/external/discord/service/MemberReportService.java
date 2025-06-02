package liaison.groble.external.discord.service;

import liaison.groble.external.discord.dto.MemberCreateReportDto;

public interface MemberReportService {

  void sendCreateMemberReport(MemberCreateReportDto memberCreateReportDto);
}
