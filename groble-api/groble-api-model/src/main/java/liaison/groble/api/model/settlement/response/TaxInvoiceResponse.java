package liaison.groble.api.model.settlement.response;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "[✅ 내 스토어 - 정산 관리 - 세금계산서 조회] 세금계산서 내역 상세 조회 응답")
public class TaxInvoiceResponse {
  private String supplierName;
  private String recipientName;
  private BigDecimal supplyAmount;
  private BigDecimal vatAmount;
  private BigDecimal totalAmount;
  private String invoiceNumber;
  private String issuedDate;
}
