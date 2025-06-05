package liaison.groble.external.discord.dto;

import lombok.Builder;

@Builder
public record BusinessMakerVerificationCreateReportDto(
    Long userId,
    String nickname,
    String bankAccountOwner,
    String bankName,
    String bankAccountNumber,
    String copyOfBankbookUrl,
    String businessType,
    String businessCategory,
    String businessSector,
    String businessName,
    String representativeName,
    String businessAddress,
    String businessLicenseFileUrl,
    String taxInvoiceEmail) {}
