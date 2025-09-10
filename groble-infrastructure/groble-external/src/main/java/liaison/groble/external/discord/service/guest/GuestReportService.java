package liaison.groble.external.discord.service.guest;

import liaison.groble.external.discord.dto.guest.GuestSignUpReportDTO;

public interface GuestReportService {
  void sendGuestSignUpReport(GuestSignUpReportDTO guestSignUpReportDTO);
}
