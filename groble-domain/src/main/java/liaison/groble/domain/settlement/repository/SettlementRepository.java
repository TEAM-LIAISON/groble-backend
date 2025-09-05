package liaison.groble.domain.settlement.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import liaison.groble.domain.settlement.entity.Settlement;

public interface SettlementRepository {
  Optional<Settlement> findByUserIdAndPeriod(
      Long sellerId, LocalDate periodStart, LocalDate periodEnd);

  Optional<Settlement> findByIdAndUserId(Long sellerId, Long settlementId);

  Optional<Settlement> findById(Long settlementId);

  BigDecimal getPendingSettlementAmount(Long sellerId);

  List<Settlement> findAllByUserId(Long userId);

  Settlement save(Settlement settlement);

  /**
   * ID 목록으로 정산들 조회 (관리자용)
   *
   * @param settlementIds 정산 ID 목록
   * @return 조회된 정산 목록
   */
  List<Settlement> findByIdIn(List<Long> settlementIds);
}
