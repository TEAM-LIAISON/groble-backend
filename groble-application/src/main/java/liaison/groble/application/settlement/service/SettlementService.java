package liaison.groble.application.settlement.service;

import java.time.LocalDate;
import java.time.YearMonth;

import org.springframework.stereotype.Service;

import liaison.groble.application.settlement.dto.SettlementDetailDTO;
import liaison.groble.application.settlement.reader.SettlementReader;
import liaison.groble.application.settlement.reader.TaxInvoiceReader;
import liaison.groble.application.user.service.UserReader;
import liaison.groble.domain.settlement.entity.Settlement;
import liaison.groble.domain.settlement.entity.TaxInvoice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementService {

  // Reader
  private final UserReader userReader;
  private final SettlementReader settlementReader;
  private final TaxInvoiceReader taxInvoiceReader;

  public SettlementDetailDTO getSettlementDetail(Long userId, YearMonth yearMonth) {
    LocalDate startDate = yearMonth.atDay(1);
    LocalDate endDate = yearMonth.atEndOfMonth();
    Settlement settlement =
        settlementReader.getSettlementByUserIdAndPeriod(userId, startDate, endDate);

    // 수동으로 관리
    Boolean isTaxInvoiceButtonEnabled = settlement.getTaxInvoiceEligible();
    // isBusinessSeller인 경우에 세금계산서 발행 가능 (모달 관리)
    Boolean isTaxInvoiceIssuable = userReader.getSellerInfoWithUser(userId).getIsBusinessSeller();
    String taxInvoiceUrl = null;

    // 세금계산서가 발행이 가능하다면
    if (settlement.getTaxInvoiceEligible()) {
      taxInvoiceUrl =
          taxInvoiceReader.getTaxInvoiceUrl(settlement.getId(), TaxInvoice.InvoiceStatus.ISSUED);
    }

    return SettlementDetailDTO.builder()
        .settlementStartDate(settlement.getSettlementStartDate())
        .settlementEndDate(settlement.getSettlementEndDate())
        .scheduledSettlementDate(settlement.getScheduledSettlementDate())
        .settlementAmount(settlement.getSettlementAmount())
        .pgFee(settlement.getPgFee())
        .platformFee(settlement.getPlatformFee())
        .isTaxInvoiceButtonEnabled(isTaxInvoiceButtonEnabled)
        .isTaxInvoiceIssuable(isTaxInvoiceIssuable)
        .taxInvoiceUrl(taxInvoiceUrl)
        .build();
  }
}
