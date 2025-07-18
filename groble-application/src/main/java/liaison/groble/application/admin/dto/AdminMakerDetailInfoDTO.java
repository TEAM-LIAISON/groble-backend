package liaison.groble.application.admin.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminMakerDetailInfoDTO {
  private Boolean isBusinessMaker;
  private String verificationStatus;
  private String bankAccountOwner;
  private String bankName;
  private String bankAccountNumber;
  private String copyOfBankBookOriginalFileName;
  private String copyOfBankbookUrl;
  private String businessType;
  private String businessCategory;
  private String businessSector;
  private String businessName;
  private String representativeName;
  private String businessAddress;
  private String businessLicenseOriginalFileName;
  private String businessLicenseFileUrl;
  private String taxInvoiceEmail;
}
