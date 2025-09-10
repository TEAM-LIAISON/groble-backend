package liaison.groble.external.discord.dto.guest;

import lombok.Builder;

@Builder
public record GuestSignUpReportDTO(Long guestId, String email, String name, String phoneNumber) {}
