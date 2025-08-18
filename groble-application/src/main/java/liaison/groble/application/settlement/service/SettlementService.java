package liaison.groble.application.settlement.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.settlement.dto.MonthlySettlementOverviewDTO;
import liaison.groble.application.settlement.dto.PerTransactionSettlementOverviewDTO;
import liaison.groble.application.settlement.dto.SettlementDetailDTO;
import liaison.groble.application.settlement.dto.SettlementOverviewDTO;
import liaison.groble.application.settlement.dto.TaxInvoiceDTO;
import liaison.groble.application.settlement.reader.SettlementReader;
import liaison.groble.application.settlement.reader.TaxInvoiceReader;
import liaison.groble.application.user.service.UserReader;
import liaison.groble.common.response.PageResponse;
import liaison.groble.domain.settlement.dto.FlatMonthlySettlement;
import liaison.groble.domain.settlement.dto.FlatPerTransactionSettlement;
import liaison.groble.domain.settlement.entity.Settlement;
import liaison.groble.domain.settlement.entity.TaxInvoice;
import liaison.groble.domain.user.entity.SellerInfo;
import liaison.groble.domain.user.enums.BusinessType;

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

  private static final ZoneId KST = ZoneId.of("Asia/Seoul");

  @Transactional
  public SettlementOverviewDTO getSettlementOverview(Long userId) {

    SellerInfo sellerInfo = userReader.getSellerInfoWithUser(userId);

    // 2) 누적 정산 금액 = 모든 Settlement의 settlementAmount 합계
    BigDecimal totalSettlementAmount =
        settlementReader.findAllByUserId(userId).stream()
            .map(Settlement::getSettlementAmount)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    // 3) 이번 달 정산 예정 금액 = 이번 달 기간 Settlement의 settlementAmount (없으면 0)
    YearMonth nowYm = YearMonth.now(KST);
    LocalDate start = nowYm.atDay(1);
    LocalDate end = nowYm.atEndOfMonth();

    BigDecimal currentMonthSettlementAmount =
        settlementReader
            .findSettlementByUserIdAndPeriod(userId, start, end)
            .map(Settlement::getSettlementAmount)
            .orElse(BigDecimal.ZERO);

    return SettlementOverviewDTO.builder()
        .verificationStatus(sellerInfo.getVerificationStatus().name())
        .totalSettlementAmount(totalSettlementAmount)
        .currentMonthSettlementAmount(currentMonthSettlementAmount)
        .build();
  }

  @Transactional(readOnly = true)
  public PageResponse<MonthlySettlementOverviewDTO> getMonthlySettlements(
      Long userId, Pageable pageable) {
    Page<FlatMonthlySettlement> page =
        settlementReader.findMonthlySettlementsByUserId(userId, pageable);

    List<MonthlySettlementOverviewDTO> items =
        page.getContent().stream().map(this::convertFlatDTOToMonthlyDTO).toList();

    PageResponse.MetaData meta =
        PageResponse.MetaData.builder()
            .sortBy(pageable.getSort().iterator().next().getProperty())
            .sortDirection(pageable.getSort().iterator().next().getDirection().name())
            .build();

    return PageResponse.from(page, items, meta);
  }

  private MonthlySettlementOverviewDTO convertFlatDTOToMonthlyDTO(FlatMonthlySettlement flat) {
    return MonthlySettlementOverviewDTO.builder()
        .settlementStartDate(flat.getSettlementStartDate())
        .settlementEndDate(flat.getSettlementEndDate())
        .settlementAmount(flat.getSettlementAmount())
        .settlementStatus(flat.getSettlementStatus())
        .build();
  }

  // 세금계산서 상세 내역 조회
  public SettlementDetailDTO getSettlementDetail(Long userId, YearMonth yearMonth) {
    LocalDate startDate = yearMonth.atDay(1);
    LocalDate endDate = yearMonth.atEndOfMonth();
    Settlement settlement =
        settlementReader.getSettlementByUserIdAndPeriod(userId, startDate, endDate);

    // 수동으로 관리
    Boolean isTaxInvoiceButtonEnabled = settlement.getTaxInvoiceEligible();
    // isBusinessSeller인 경우에 세금계산서 발행 가능 (모달 관리)
    SellerInfo sellerInfo = userReader.getSellerInfoWithUser(userId);
    boolean isTaxInvoiceIssuable =
        sellerInfo != null
            && Boolean.TRUE.equals(sellerInfo.getIsBusinessSeller()) // 사업자 판매자
            && (sellerInfo.getBusinessType() == BusinessType.CORPORATE
                || sellerInfo.getBusinessType() == BusinessType.INDIVIDUAL_NORMAL);

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
        .vatAmount(settlement.getFeeVat())
        .isTaxInvoiceButtonEnabled(isTaxInvoiceButtonEnabled)
        .isTaxInvoiceIssuable(isTaxInvoiceIssuable)
        .taxInvoiceUrl(taxInvoiceUrl)
        .build();
  }

  @Transactional
  public PageResponse<PerTransactionSettlementOverviewDTO> getPerTransactionSettlements(
      Long userId, YearMonth yearMonth, Pageable pageable) {
    LocalDate startDate = yearMonth.atDay(1);
    LocalDate endDate = yearMonth.atEndOfMonth();

    Page<FlatPerTransactionSettlement> page =
        settlementReader.findPerTransactionSettlementsByUserIdAndYearMonth(
            userId, startDate, endDate, pageable);

    List<PerTransactionSettlementOverviewDTO> items =
        page.getContent().stream().map(this::convertFlatDTOToPerTransactionDTO).toList();

    PageResponse.MetaData meta =
        PageResponse.MetaData.builder()
            .sortBy(pageable.getSort().iterator().next().getProperty())
            .sortDirection(pageable.getSort().iterator().next().getDirection().name())
            .build();

    log.info(
        "Per transaction settlements retrieved for userId: {}, yearMonth: {}, page: {}, size: {}, totalElements: {}",
        userId,
        yearMonth,
        pageable.getPageNumber(),
        pageable.getPageSize(),
        page.getTotalElements());

    return PageResponse.from(page, items, meta);
  }

  @Transactional(readOnly = true)
  public TaxInvoiceDTO getTaxInvoice(Long userId, YearMonth yearMonth) {

    SellerInfo sellerInfo = userReader.getSellerInfoWithUser(userId);
    TaxInvoice taxInvoice = taxInvoiceReader.findByUserAndYearMonth(userId, yearMonth);

    return TaxInvoiceDTO.builder()
        .supplierName("리에종")
        .recipientName(sellerInfo.getBusinessName())
        .supplyAmount(taxInvoice.getSupplyAmount())
        .vatAmount(taxInvoice.getVatAmount())
        .totalAmount(taxInvoice.getTotalAmount())
        .invoiceNumber(taxInvoice.getInvoiceNumber())
        .issuedDate(taxInvoice.getIssuedDate())
        .taxInvoiceUrl(taxInvoice.getInvoiceUrl())
        .build();
  }

  private PerTransactionSettlementOverviewDTO convertFlatDTOToPerTransactionDTO(
      FlatPerTransactionSettlement flat) {
    return PerTransactionSettlementOverviewDTO.builder()
        .contentTitle(flat.getContentTitle())
        .settlementAmount(flat.getSettlementAmount())
        .purchasedAt(flat.getPurchasedAt())
        .build();
  }
}
