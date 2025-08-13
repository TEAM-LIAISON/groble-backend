package liaison.groble.persistence.settlement;

import org.springframework.data.jpa.repository.JpaRepository;

import liaison.groble.domain.settlement.entity.TaxInvoice;

public interface JpaTaxInvoiceRepository extends JpaRepository<TaxInvoice, Long> {}
