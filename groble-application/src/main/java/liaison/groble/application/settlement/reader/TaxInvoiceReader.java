package liaison.groble.application.settlement.reader;

import java.time.YearMonth;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.common.exception.EntityNotFoundException;
import liaison.groble.domain.settlement.entity.TaxInvoice;
import liaison.groble.domain.settlement.repository.TaxInvoiceCustomRepository;
import liaison.groble.domain.settlement.repository.TaxInvoiceRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 세금계산서 조회 담당 전용 컴포넌트
 *
 * <p>모든 세금계산서 조회 로직을 중앙화하여 일관성 있는 조회 및 예외 처리를 제공
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaxInvoiceReader {
  private final TaxInvoiceRepository taxInvoiceRepository;
  private final TaxInvoiceCustomRepository taxInvoiceCustomRepository;

  public Optional<TaxInvoice> findFirstBySettlementIdAndStatusOrderByIdDesc(
      Long settlementId, TaxInvoice.InvoiceStatus status) {
    return taxInvoiceRepository.findFirstBySettlementIdAndStatusOrderByIdDesc(settlementId, status);
  }

  public String getTaxInvoiceUrl(Long settlementId, TaxInvoice.InvoiceStatus status) {
    return findFirstBySettlementIdAndStatusOrderByIdDesc(settlementId, status)
        .map(TaxInvoice::getInvoiceUrl)
        .orElseThrow(() -> new EntityNotFoundException("세금계산서 URL을 찾을 수 없습니다."));
  }

  public TaxInvoice findByUserAndYearMonth(Long userId, YearMonth yearMonth) {
    return taxInvoiceCustomRepository
        .findByUserAndYearMonth(userId, yearMonth)
        .orElseThrow(() -> new EntityNotFoundException("해당 연월의 세금계산서를 찾을 수 없습니다."));
  }
}
