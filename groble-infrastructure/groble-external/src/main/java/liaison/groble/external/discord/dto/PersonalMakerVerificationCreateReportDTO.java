package liaison.groble.external.discord.dto;

import lombok.Builder;

@Builder
public record PersonalMakerVerificationCreateReportDTO(
    Long userId,
    String nickname,
    String bankAccountOwner,
    String bankName,
    String bankAccountNumber,
    String copyOfBankbookUrl) {}
