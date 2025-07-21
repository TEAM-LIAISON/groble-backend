package liaison.groble.external.discord.dto;

import java.time.LocalDateTime;

import lombok.Builder;

@Builder
public record MemberCreateReportDTO(Long userId, String nickname, LocalDateTime createdAt) {}
