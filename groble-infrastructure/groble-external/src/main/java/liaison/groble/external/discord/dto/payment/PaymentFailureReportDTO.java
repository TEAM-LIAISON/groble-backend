package liaison.groble.external.discord.dto.payment;

import java.math.BigDecimal;

import lombok.Builder;

@Builder
public record PaymentFailureReportDTO(
    String buyerName,
    String productName,
    String productOptionName,
    BigDecimal price,
    String failureReason) {}
