package liaison.groble.application.admin.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminMakerDetailInfoDto {
  private Boolean isBusinessMaker;
  private String verificationStatus;
  private String bankAccountOwner;
  private String bankName;
  private String bankAccountNumber;
  private String copyOfBankbookUrl;
  private String businessType;
  private String businessCategory;
  private String businessSector;
  private String businessName;
  private String representativeName;
  private String businessAddress;
  private String businessLicenseFileUrl;
  private String taxInvoiceEmail;
}
