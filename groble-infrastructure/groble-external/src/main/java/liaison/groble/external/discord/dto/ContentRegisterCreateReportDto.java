package liaison.groble.external.discord.dto;

import java.time.LocalDateTime;

import lombok.Builder;

@Builder
public record ContentRegisterCreateReportDto(
    String nickname,
    Long contentId,
    String contentTitle,
    String contentType,
    LocalDateTime createdAt) {}
