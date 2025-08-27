package liaison.groble.persistence.settlement;

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
      @Param("settlementId") Long settlementId, @Param("userId") Long userId);

  List<Settlement> findAllByUserId(Long userId);
}
