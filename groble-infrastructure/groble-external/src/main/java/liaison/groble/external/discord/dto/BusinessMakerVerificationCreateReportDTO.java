package liaison.groble.external.discord.dto;

import lombok.Builder;

@Builder
public record BusinessMakerVerificationCreateReportDTO(
    Long userId,
    String nickname,
    String bankAccountOwner,
    String birthDate,
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
