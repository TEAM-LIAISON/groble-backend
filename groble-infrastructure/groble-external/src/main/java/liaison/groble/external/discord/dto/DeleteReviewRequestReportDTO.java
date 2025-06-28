package liaison.groble.external.discord.dto;

import lombok.Builder;

@Builder
public record DeleteReviewRequestReportDTO(Long userId, Long reviewId) {}
