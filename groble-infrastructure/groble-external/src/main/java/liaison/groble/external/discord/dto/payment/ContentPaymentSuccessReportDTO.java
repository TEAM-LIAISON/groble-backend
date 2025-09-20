package liaison.groble.external.discord.dto.payment;

import java.time.LocalDateTime;

import lombok.Builder;

@Builder
public record ContentPaymentSuccessReportDTO(
    Long userId,
    String nickname,
    Long guestUserId,
    String guestUserName,
    Long contentId,
    String contentTitle,
    String contentType,
    Long optionId,
    String selectedOptionName,
    String merchantUid,
    LocalDateTime purchasedAt) {}
