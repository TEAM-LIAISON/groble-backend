package liaison.groble.external.discord.service.guest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import liaison.groble.external.discord.dto.guest.GuestSignUpReportDTO;
import liaison.groble.external.discord.service.DiscordService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DiscordGuestReportService implements GuestReportService {

  @Value("${discord.webhook.alert.guest-member.url}")
  private String url;

  private final DiscordService discordService;

  @Override
  public void sendGuestSignUpReport(GuestSignUpReportDTO guestSignUpReportDTO) {
    var msg =
        String.format(
            """
        ## 🆕 신규 비회원 가입 알림

        **가입 정보**
        • **ID**: `%d`
        • **이름**: %s
        • **이메일**: %s
        • **연락처**: %s

        **가입 시간**: %s
        """,
            guestSignUpReportDTO.guestId(),
            guestSignUpReportDTO.name(),
            guestSignUpReportDTO.email(),
            guestSignUpReportDTO.phoneNumber(),
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
  }
}
