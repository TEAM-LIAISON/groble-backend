package liaison.groble.persistence.settlement;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import liaison.groble.domain.settlement.entity.Settlement;
import liaison.groble.domain.settlement.repository.SettlementRepository;

import lombok.AllArgsConstructor;

@Repository
@AllArgsConstructor
public class SettlementRepositoryImpl implements SettlementRepository {
  private final JpaSettlementRepository jpaSettlementRepository;

  @Override
  public Optional<Settlement> findByUserIdAndPeriod(
      Long sellerId, LocalDate periodStart, LocalDate periodEnd) {
    return jpaSettlementRepository.findByUserIdAndPeriod(sellerId, periodStart, periodEnd);
  }

  @Override
  public Settlement save(Settlement settlement) {
    return jpaSettlementRepository.save(settlement);
  }
}
