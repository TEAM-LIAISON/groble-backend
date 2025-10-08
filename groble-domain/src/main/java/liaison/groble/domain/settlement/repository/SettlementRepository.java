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

  /**
   * 페이플 빌링키로 정산 조회 (웹훅 처리용)
   *
   * @param billingTranId 페이플 빌링키
   * @return 해당 빌링키를 가진 정산 목록
   */
  List<Settlement> findByPaypleBillingTranId(String billingTranId);

  /** COMPLETED 상태 정산들의 PG 수수료 환급 예상액 합계 */
  BigDecimal sumPgFeeRefundExpectedForCompleted();
}
