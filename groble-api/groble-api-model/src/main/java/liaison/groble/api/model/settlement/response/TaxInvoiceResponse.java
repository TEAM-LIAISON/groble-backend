package liaison.groble.api.model.settlement.response;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "[✅ 내 스토어 - 정산 관리 - 세금계산서 조회] 세금계산서 내역 상세 조회 응답")
public class TaxInvoiceResponse {

  @Schema(
      description = "공급자 이름",
      example = "리에종",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String supplierName;

  @Schema(
      description = "공급받는 자",
      example = "회사명",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String recipientName;

  @Schema(
      description = "공급가액 (원화 표기)",
      example = "1000000",
      type = "number",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private BigDecimal supplyAmount;

  @Schema(
      description = "부가세 (원화 표기)",
      example = "100000",
      type = "number",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private BigDecimal vatAmount;

  @Schema(
      description = "합계 (공급가액 + VAT)",
      example = "1100000",
      type = "number",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private BigDecimal totalAmount;

  @Schema(
      description = "세금계산서 발행번호",
      example = "2023-0001",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String invoiceNumber;

  @Schema(
      description = "발행일 (YYYY-MM-DD)",
      example = "2023-01-01",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonFormat(pattern = "yyyy-MM-dd")
  private LocalDate issuedDate;

  @Schema(
      description = "세금계산서 URL",
      example = "https://example.com/tax-invoice/2023-0001",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String taxInvoiceUrl;
}
