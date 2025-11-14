package liaison.groble.domain.settlement.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlatAdminSettlementsDTO {
  private Long settlementId;
  private LocalDate scheduledSettlementDate;
  private String contentType;
  private BigDecimal settlementAmount;
  private BigDecimal settlementAmountDisplay;
  private String settlementStatus;
  private String paymentType;

  // SellerInfo 존재
  private String nickname;
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
