package liaison.groble.domain.settlement.repository;

import java.time.YearMonth;
import java.util.Optional;

import liaison.groble.domain.settlement.entity.TaxInvoice;

public interface TaxInvoiceCustomRepository {
  Optional<TaxInvoice> findByUserAndYearMonth(Long userId, YearMonth yearMonth);
}
