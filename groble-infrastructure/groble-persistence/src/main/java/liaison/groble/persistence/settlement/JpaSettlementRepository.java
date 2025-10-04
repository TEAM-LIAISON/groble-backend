package liaison.groble.persistence.settlement;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import liaison.groble.domain.settlement.entity.Settlement;

public interface JpaSettlementRepository extends JpaRepository<Settlement, Long> {

  @Query(
      "SELECT s FROM Settlement s WHERE s.user.id = :userId "
          + "AND s.settlementStartDate = :startDate "
          + "AND s.settlementEndDate = :endDate")
  Optional<Settlement> findByUserIdAndPeriod(
      @Param("userId") Long userId,
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate);

  @Query("SELECT s FROM Settlement s WHERE s.id = :settlementId AND s.user.id = :userId")
  Optional<Settlement> findByIdAndUserId(
      @Param("userId") Long userId, @Param("settlementId") Long settlementId);

  Optional<Settlement> findById(Long settlementId);

  @Query(
      "SELECT COALESCE(SUM(s.settlementAmountDisplay), 0) FROM Settlement s "
          + "WHERE s.user.id = :sellerId "
          + "AND s.status = 'PENDING'")
  BigDecimal calculatePendingSettlementAmount(@Param("sellerId") Long sellerId);

  List<Settlement> findAllByUserId(Long userId);

  List<Settlement> findByIdIn(List<Long> settlementIds);

  List<Settlement> findByPaypleBillingTranId(String billingTranId);
}
