package liaison.groble.external.discord.service.member;

import liaison.groble.external.discord.dto.MemberCreateReportDTO;

public interface MemberReportService {

  void sendCreateMemberReport(MemberCreateReportDTO memberCreateReportDTO);
}
