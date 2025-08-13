package liaison.groble.persistence.settlement;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import liaison.groble.domain.settlement.entity.TaxInvoice;

public interface JpaTaxInvoiceRepository extends JpaRepository<TaxInvoice, Long> {

  Optional<TaxInvoice> findFirstBySettlementIdAndStatusOrderByIdDesc(
      Long settlementId, TaxInvoice.InvoiceStatus status);
}
