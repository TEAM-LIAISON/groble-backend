package liaison.groble.application.settlement.service;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.settlement.dto.PerTransactionSettlementOverviewDTO;
import liaison.groble.application.settlement.dto.SettlementDetailDTO;
import liaison.groble.application.settlement.dto.SettlementOverviewDTO;
import liaison.groble.application.settlement.dto.SettlementsOverviewDTO;
import liaison.groble.application.settlement.dto.TaxInvoiceDTO;
import liaison.groble.application.settlement.reader.SettlementReader;
import liaison.groble.application.settlement.reader.TaxInvoiceReader;
import liaison.groble.application.user.service.UserReader;
import liaison.groble.common.response.PageResponse;
import liaison.groble.domain.content.enums.ContentPaymentType;
import liaison.groble.domain.settlement.dto.FlatPerTransactionSettlement;
import liaison.groble.domain.settlement.dto.FlatSettlementsDTO;
import liaison.groble.domain.settlement.entity.Settlement;
import liaison.groble.domain.settlement.entity.SettlementItem;
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

  @Transactional
  public SettlementOverviewDTO getSettlementOverview(Long userId) {

    SellerInfo sellerInfo = userReader.getSellerInfoWithUser(userId);

    // 2) 누적 정산 금액 = 모든 Settlement의 settlementAmount 합계
    BigDecimal totalSettlementAmount =
        settlementReader.findAllByUserId(userId).stream()
            .map(Settlement::getSettlementAmountDisplay)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal pendingSettlementAmount = settlementReader.getPendingSettlementAmount(userId);

    return SettlementOverviewDTO.builder()
        .verificationStatus(sellerInfo.getVerificationStatus().name())
        .totalSettlementAmount(totalSettlementAmount)
        .pendingSettlementAmount(pendingSettlementAmount)
        .build();
  }

  @Transactional(readOnly = true)
  public PageResponse<SettlementsOverviewDTO> getMonthlySettlements(
      Long userId, Pageable pageable) {
    Page<FlatSettlementsDTO> page = settlementReader.findSettlementsByUserId(userId, pageable);

    List<SettlementsOverviewDTO> items =
        page.getContent().stream().map(this::convertFlatDTOToSettlementsDTO).toList();

    PageResponse.MetaData meta =
        PageResponse.MetaData.builder()
            .sortBy(pageable.getSort().iterator().next().getProperty())
            .sortDirection(pageable.getSort().iterator().next().getDirection().name())
            .build();

    return PageResponse.from(page, items, meta);
  }

  private SettlementsOverviewDTO convertFlatDTOToSettlementsDTO(FlatSettlementsDTO flat) {
    return SettlementsOverviewDTO.builder()
        .settlementId(flat.getSettlementId())
        .settlementStartDate(flat.getSettlementStartDate())
        .settlementEndDate(flat.getSettlementEndDate())
        .scheduledSettlementDate(flat.getScheduledSettlementDate())
        .contentType(flat.getContentType())
        .paymentType(flat.getPaymentType())
        .settlementAmount(flat.getSettlementAmountDisplay())
        .settlementStatus(
            resolveDisplayStatus(flat.getSettlementStatus(), flat.getSettlementAmountDisplay()))
        .build();
  }

  // 세금계산서 상세 내역 조회
  public SettlementDetailDTO getSettlementDetail(Long userId, Long settlementId) {
    Settlement settlement = settlementReader.getSettlementByIdAndUserId(userId, settlementId);

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

    boolean hasSubscriptionSettlement =
        settlement.getSettlementItems().stream()
            .filter(Objects::nonNull)
            .anyMatch(SettlementItem::isSubscriptionSettlement);

    String paymentType =
        hasSubscriptionSettlement
            ? ContentPaymentType.SUBSCRIPTION.name()
            : ContentPaymentType.ONE_TIME.name();

    BigDecimal displayPlatformFee = nullSafe(settlement.getPlatformFeeDisplay());
    BigDecimal displayPgFee = nullSafe(settlement.getPgFeeDisplay());
    BigDecimal displayVat = nullSafe(settlement.getFeeVatDisplay());
    BigDecimal displayTotalFee = nullSafe(settlement.getTotalFeeDisplay());
    BigDecimal displaySettlementAmount = nullSafe(settlement.getSettlementAmountDisplay());
    BigDecimal pgFeeRefundExpected = nullSafe(settlement.getPgFeeRefundExpected());

    return SettlementDetailDTO.builder()
        .settlementStartDate(settlement.getSettlementStartDate())
        .settlementEndDate(settlement.getSettlementEndDate())
        .scheduledSettlementDate(settlement.getScheduledSettlementDate())
        .paymentType(paymentType)
        .settlementAmount(displaySettlementAmount)
        .pgFee(displayPgFee)
        .pgFeeRefundExpected(pgFeeRefundExpected)
        .platformFee(displayPlatformFee)
        .platformFeeForgone(settlement.getPlatformFeeForgone())
        .vatAmount(displayVat)
        .isTaxInvoiceButtonEnabled(isTaxInvoiceButtonEnabled)
        .isTaxInvoiceIssuable(isTaxInvoiceIssuable)
        .taxInvoiceUrl(taxInvoiceUrl)
        .build();
  }

  private BigDecimal nullSafe(BigDecimal value) {
    return value != null ? value : BigDecimal.ZERO;
  }

  private String resolveDisplayStatus(String originalStatus, BigDecimal amount) {
    if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) {
      return Settlement.SettlementStatus.NOT_APPLICABLE.name();
    }
    return originalStatus;
  }

  @Transactional
  public PageResponse<PerTransactionSettlementOverviewDTO> getPerTransactionSettlements(
      Long userId, Long settlementId, Pageable pageable) {

    Page<FlatPerTransactionSettlement> page =
        settlementReader.findPerTransactionSettlementsByIdAndUserId(userId, settlementId, pageable);

    List<PerTransactionSettlementOverviewDTO> items =
        page.getContent().stream().map(this::convertFlatDTOToPerTransactionDTO).toList();

    PageResponse.MetaData meta =
        PageResponse.MetaData.builder()
            .sortBy(pageable.getSort().iterator().next().getProperty())
            .sortDirection(pageable.getSort().iterator().next().getDirection().name())
            .build();

    log.info(
        "Per transaction settlements retrieved for userId: {}, settlementId: {}, page: {}, size: {}, totalElements: {}",
        userId,
        settlementId,
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
        .settlementAmount(flat.getSettlementAmountDisplay())
        .orderStatus(flat.getOrderStatus())
        .purchasedAt(flat.getPurchasedAt())
        .build();
  }
}
