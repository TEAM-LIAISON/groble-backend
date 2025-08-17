package liaison.groble.application.settlement.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TaxInvoiceDTO {
  private String supplierName;
  private String recipientName;
  private BigDecimal supplyAmount;
  private BigDecimal vatAmount;
  private BigDecimal totalAmount;
  private String invoiceNumber;
  private LocalDate issuedDate;
  private String taxInvoiceUrl;
}
