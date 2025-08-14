package liaison.groble.domain.settlement.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import liaison.groble.domain.settlement.entity.Settlement;

public interface SettlementRepository {
  Optional<Settlement> findByUserIdAndPeriod(
      Long sellerId, LocalDate periodStart, LocalDate periodEnd);

  List<Settlement> findAllByUserId(Long userId);

  Settlement save(Settlement settlement);
}
