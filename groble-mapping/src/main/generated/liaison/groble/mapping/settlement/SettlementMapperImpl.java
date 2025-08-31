package liaison.groble.mapping.settlement;

import javax.annotation.processing.Generated;

import org.springframework.stereotype.Component;

import liaison.groble.api.model.settlement.response.PerTransactionSettlementOverviewResponse;
import liaison.groble.api.model.settlement.response.SettlementDetailResponse;
import liaison.groble.api.model.settlement.response.SettlementOverviewResponse;
import liaison.groble.api.model.settlement.response.SettlementsOverviewResponse;
import liaison.groble.api.model.settlement.response.TaxInvoiceResponse;
import liaison.groble.application.settlement.dto.PerTransactionSettlementOverviewDTO;
import liaison.groble.application.settlement.dto.SettlementDetailDTO;
import liaison.groble.application.settlement.dto.SettlementOverviewDTO;
import liaison.groble.application.settlement.dto.SettlementsOverviewDTO;
import liaison.groble.application.settlement.dto.TaxInvoiceDTO;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-08-30T20:20:02+0900",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.10 (Amazon.com Inc.)")
@Component
public class SettlementMapperImpl implements SettlementMapper {

  @Override
  public SettlementDetailResponse toSettlementResponse(SettlementDetailDTO settlementDetailDTO) {
    if (settlementDetailDTO == null) {
      return null;
    }

    SettlementDetailResponse.SettlementDetailResponseBuilder settlementDetailResponse =
        SettlementDetailResponse.builder();

    if (settlementDetailDTO.getSettlementStartDate() != null) {
      settlementDetailResponse.settlementStartDate(settlementDetailDTO.getSettlementStartDate());
    }
    if (settlementDetailDTO.getSettlementEndDate() != null) {
      settlementDetailResponse.settlementEndDate(settlementDetailDTO.getSettlementEndDate());
    }
    if (settlementDetailDTO.getScheduledSettlementDate() != null) {
      settlementDetailResponse.scheduledSettlementDate(
          settlementDetailDTO.getScheduledSettlementDate());
    }
    if (settlementDetailDTO.getSettlementAmount() != null) {
      settlementDetailResponse.settlementAmount(settlementDetailDTO.getSettlementAmount());
    }
    if (settlementDetailDTO.getPgFee() != null) {
      settlementDetailResponse.pgFee(settlementDetailDTO.getPgFee());
    }
    if (settlementDetailDTO.getPlatformFee() != null) {
      settlementDetailResponse.platformFee(settlementDetailDTO.getPlatformFee());
    }
    if (settlementDetailDTO.getVatAmount() != null) {
      settlementDetailResponse.vatAmount(settlementDetailDTO.getVatAmount());
    }
    if (settlementDetailDTO.getIsTaxInvoiceButtonEnabled() != null) {
      settlementDetailResponse.isTaxInvoiceButtonEnabled(
          settlementDetailDTO.getIsTaxInvoiceButtonEnabled());
    }
    if (settlementDetailDTO.getIsTaxInvoiceIssuable() != null) {
      settlementDetailResponse.isTaxInvoiceIssuable(settlementDetailDTO.getIsTaxInvoiceIssuable());
    }
    if (settlementDetailDTO.getTaxInvoiceUrl() != null) {
      settlementDetailResponse.taxInvoiceUrl(settlementDetailDTO.getTaxInvoiceUrl());
    }

    return settlementDetailResponse.build();
  }

  @Override
  public SettlementOverviewResponse toSettlementOverviewResponse(
      SettlementOverviewDTO settlementOverviewDTO) {
    if (settlementOverviewDTO == null) {
      return null;
    }

    SettlementOverviewResponse.SettlementOverviewResponseBuilder settlementOverviewResponse =
        SettlementOverviewResponse.builder();

    if (settlementOverviewDTO.getVerificationStatus() != null) {
      settlementOverviewResponse.verificationStatus(settlementOverviewDTO.getVerificationStatus());
    }
    if (settlementOverviewDTO.getTotalSettlementAmount() != null) {
      settlementOverviewResponse.totalSettlementAmount(
          settlementOverviewDTO.getTotalSettlementAmount());
    }
    if (settlementOverviewDTO.getPendingSettlementAmount() != null) {
      settlementOverviewResponse.pendingSettlementAmount(
          settlementOverviewDTO.getPendingSettlementAmount());
    }

    return settlementOverviewResponse.build();
  }

  @Override
  public SettlementsOverviewResponse toSettlementsOverviewResponse(
      SettlementsOverviewDTO settlementsOverviewDTO) {
    if (settlementsOverviewDTO == null) {
      return null;
    }

    SettlementsOverviewResponse.SettlementsOverviewResponseBuilder settlementsOverviewResponse =
        SettlementsOverviewResponse.builder();

    if (settlementsOverviewDTO.getSettlementId() != null) {
      settlementsOverviewResponse.settlementId(settlementsOverviewDTO.getSettlementId());
    }
    if (settlementsOverviewDTO.getSettlementStartDate() != null) {
      settlementsOverviewResponse.settlementStartDate(
          settlementsOverviewDTO.getSettlementStartDate());
    }
    if (settlementsOverviewDTO.getSettlementEndDate() != null) {
      settlementsOverviewResponse.settlementEndDate(settlementsOverviewDTO.getSettlementEndDate());
    }
    if (settlementsOverviewDTO.getScheduledSettlementDate() != null) {
      settlementsOverviewResponse.scheduledSettlementDate(
          settlementsOverviewDTO.getScheduledSettlementDate());
    }
    if (settlementsOverviewDTO.getContentType() != null) {
      settlementsOverviewResponse.contentType(settlementsOverviewDTO.getContentType());
    }
    if (settlementsOverviewDTO.getSettlementAmount() != null) {
      settlementsOverviewResponse.settlementAmount(settlementsOverviewDTO.getSettlementAmount());
    }
    if (settlementsOverviewDTO.getSettlementStatus() != null) {
      settlementsOverviewResponse.settlementStatus(settlementsOverviewDTO.getSettlementStatus());
    }

    return settlementsOverviewResponse.build();
  }

  @Override
  public PerTransactionSettlementOverviewResponse toPerTransactionSettlementOverviewResponse(
      PerTransactionSettlementOverviewDTO perTransactionSettlementOverviewDTO) {
    if (perTransactionSettlementOverviewDTO == null) {
      return null;
    }

    PerTransactionSettlementOverviewResponse.PerTransactionSettlementOverviewResponseBuilder
        perTransactionSettlementOverviewResponse =
            PerTransactionSettlementOverviewResponse.builder();

    if (perTransactionSettlementOverviewDTO.getContentTitle() != null) {
      perTransactionSettlementOverviewResponse.contentTitle(
          perTransactionSettlementOverviewDTO.getContentTitle());
    }
    if (perTransactionSettlementOverviewDTO.getSettlementAmount() != null) {
      perTransactionSettlementOverviewResponse.settlementAmount(
          perTransactionSettlementOverviewDTO.getSettlementAmount());
    }
    if (perTransactionSettlementOverviewDTO.getPurchasedAt() != null) {
      perTransactionSettlementOverviewResponse.purchasedAt(
          perTransactionSettlementOverviewDTO.getPurchasedAt());
    }

    return perTransactionSettlementOverviewResponse.build();
  }

  @Override
  public TaxInvoiceResponse toTaxInvoiceResponse(TaxInvoiceDTO taxInvoiceDTO) {
    if (taxInvoiceDTO == null) {
      return null;
    }

    TaxInvoiceResponse.TaxInvoiceResponseBuilder taxInvoiceResponse = TaxInvoiceResponse.builder();

    if (taxInvoiceDTO.getSupplierName() != null) {
      taxInvoiceResponse.supplierName(taxInvoiceDTO.getSupplierName());
    }
    if (taxInvoiceDTO.getRecipientName() != null) {
      taxInvoiceResponse.recipientName(taxInvoiceDTO.getRecipientName());
    }
    if (taxInvoiceDTO.getSupplyAmount() != null) {
      taxInvoiceResponse.supplyAmount(taxInvoiceDTO.getSupplyAmount());
    }
    if (taxInvoiceDTO.getVatAmount() != null) {
      taxInvoiceResponse.vatAmount(taxInvoiceDTO.getVatAmount());
    }
    if (taxInvoiceDTO.getTotalAmount() != null) {
      taxInvoiceResponse.totalAmount(taxInvoiceDTO.getTotalAmount());
    }
    if (taxInvoiceDTO.getInvoiceNumber() != null) {
      taxInvoiceResponse.invoiceNumber(taxInvoiceDTO.getInvoiceNumber());
    }
    if (taxInvoiceDTO.getIssuedDate() != null) {
      taxInvoiceResponse.issuedDate(taxInvoiceDTO.getIssuedDate());
    }
    if (taxInvoiceDTO.getTaxInvoiceUrl() != null) {
      taxInvoiceResponse.taxInvoiceUrl(taxInvoiceDTO.getTaxInvoiceUrl());
    }

    return taxInvoiceResponse.build();
  }
}
