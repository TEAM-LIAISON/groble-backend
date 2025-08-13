package liaison.groble.persistence.settlement;

import org.springframework.stereotype.Repository;

import liaison.groble.domain.settlement.repository.TaxInvoiceRepository;

import lombok.AllArgsConstructor;

@Repository
@AllArgsConstructor
public class TaxInvoiceRepositoryImpl implements TaxInvoiceRepository {
  private final JpaTaxInvoiceRepository jpaTaxInvoiceRepository;
}
