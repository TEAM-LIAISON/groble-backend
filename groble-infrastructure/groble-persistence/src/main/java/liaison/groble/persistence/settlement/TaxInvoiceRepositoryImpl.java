package liaison.groble.persistence.settlement;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import liaison.groble.domain.settlement.entity.TaxInvoice;
import liaison.groble.domain.settlement.repository.TaxInvoiceRepository;

import lombok.AllArgsConstructor;

@Repository
@AllArgsConstructor
public class TaxInvoiceRepositoryImpl implements TaxInvoiceRepository {
  private final JpaTaxInvoiceRepository jpaTaxInvoiceRepository;

  @Override
  public Optional<TaxInvoice> findFirstBySettlementIdAndStatusOrderByIdDesc(
      Long settlementId, TaxInvoice.InvoiceStatus status) {
    return jpaTaxInvoiceRepository.findFirstBySettlementIdAndStatusOrderByIdDesc(
        settlementId, status);
  }
}
