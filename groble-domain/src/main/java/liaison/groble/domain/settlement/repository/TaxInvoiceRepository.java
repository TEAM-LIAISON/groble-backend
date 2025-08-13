package liaison.groble.domain.settlement.repository;

import java.util.Optional;

import liaison.groble.domain.settlement.entity.TaxInvoice;

public interface TaxInvoiceRepository {
  Optional<TaxInvoice> findFirstBySettlementIdAndStatusOrderByIdDesc(
      Long settlementId, TaxInvoice.InvoiceStatus status);
}
