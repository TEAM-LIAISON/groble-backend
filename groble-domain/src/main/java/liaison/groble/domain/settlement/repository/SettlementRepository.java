package liaison.groble.domain.settlement.repository;

import java.time.LocalDate;
import java.util.Optional;

import liaison.groble.domain.settlement.entity.Settlement;

public interface SettlementRepository {
  Optional<Settlement> findByUserIdAndPeriod(
      Long sellerId, LocalDate periodStart, LocalDate periodEnd);

  Settlement save(Settlement settlement);
}
