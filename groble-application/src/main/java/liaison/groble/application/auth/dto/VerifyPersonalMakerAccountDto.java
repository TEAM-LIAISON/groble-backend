package liaison.groble.application.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VerifyPersonalMakerAccountDto {
  private final String bankAccountOwner;
  private final String bankName;
  private final String bankAccountNumber;
  private final String copyOfBankbookUrl;
}
