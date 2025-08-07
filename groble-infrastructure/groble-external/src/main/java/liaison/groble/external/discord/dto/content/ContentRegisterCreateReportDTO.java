package liaison.groble.external.discord.dto.content;

import java.time.LocalDateTime;

import lombok.Builder;

@Builder
public record ContentRegisterCreateReportDTO(
    String nickname,
    Long contentId,
    String contentTitle,
    String contentType,
    LocalDateTime createdAt) {}
