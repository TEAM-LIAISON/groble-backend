package liaison.groble.application.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VerifyBusinessMakerAccountDTO {
  private final String bankAccountOwner;
  private final String bankName;
  private final String bankAccountNumber;
  private final String copyOfBankbookUrl;
  private final BusinessType businessType;
  private final String businessCategory;
  private final String businessSector;
  private final String businessName;
  private final String representativeName;
  private final String businessAddress;
  private final String businessLicenseFileUrl;
  private final String taxInvoiceEmail;

  public enum BusinessType {
    INDIVIDUAL_SIMPLIFIED,
    INDIVIDUAL_NORMAL,
    CORPORATE
  }
}
