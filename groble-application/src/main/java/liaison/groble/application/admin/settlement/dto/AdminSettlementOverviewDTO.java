package liaison.groble.application.admin.settlement.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminSettlementOverviewDTO {
  private Long settlementId;
  private LocalDate scheduledSettlementDate;
  private String contentType;
  private BigDecimal settlementAmount;
  private String settlementStatus;
  private String verificationStatus;
  private Boolean isBusinessSeller;
  private String businessType;
  private String bankAccountOwner;
  private String bankName;
  private String bankAccountNumber;
  private String copyOfBankbookUrl;
  private String businessLicenseFileUrl;
  private String taxInvoiceEmail;
}
