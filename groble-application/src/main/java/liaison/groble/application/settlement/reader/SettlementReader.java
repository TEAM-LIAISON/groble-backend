package liaison.groble.application.settlement.reader;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.common.exception.EntityNotFoundException;
import liaison.groble.domain.settlement.dto.FlatAdminSettlementsDTO;
import liaison.groble.domain.settlement.dto.FlatPerTransactionSettlement;
import liaison.groble.domain.settlement.dto.FlatSettlementsDTO;
import liaison.groble.domain.settlement.entity.Settlement;
import liaison.groble.domain.settlement.entity.SettlementItem;
import liaison.groble.domain.settlement.repository.SettlementCustomRepository;
import liaison.groble.domain.settlement.repository.SettlementItemRepository;
import liaison.groble.domain.settlement.repository.SettlementRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 정산 조회 담당 전용 컴포넌트
 *
 * <p>모든 정산 조회 로직을 중앙화하여 일관성 있는 조회 및 예외 처리를 제공
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SettlementReader {
  private final SettlementRepository settlementRepository;
  private final SettlementCustomRepository settlementCustomRepository;
  private final SettlementItemRepository settlementItemRepository;

  /**
   * 정산 정보 조회 (필수)
   *
   * @throws EntityNotFoundException 정산 정보가 없는 경우
   */
  public Settlement getSettlementByUserIdAndPeriod(
      Long sellerId, LocalDate periodStart, LocalDate periodEnd) {
    return settlementRepository
        .findByUserIdAndPeriod(sellerId, periodStart, periodEnd)
        .orElseThrow(
            () ->
                new EntityNotFoundException(
                    String.format(
                        "정산 정보를 찾을 수 없습니다 - sellerId: %d, period: %s ~ %s",
                        sellerId, periodStart, periodEnd)));
  }

  public Settlement getSettlementByIdAndUserId(Long sellerId, Long settlementId) {
    return settlementRepository
        .findByIdAndUserId(sellerId, settlementId)
        .orElseThrow(
            () ->
                new EntityNotFoundException(
                    String.format(
                        "정산 정보를 찾을 수 없습니다 - sellerId: %d, settlementId: %d",
                        sellerId, settlementId)));
  }

  /**
   * 정산 정보 조회 (Optional)
   *
   * @return 정산 정보 Optional
   */
  public Optional<Settlement> findSettlementByUserIdAndPeriod(
      Long sellerId, LocalDate periodStart, LocalDate periodEnd) {
    return settlementRepository.findByUserIdAndPeriod(sellerId, periodStart, periodEnd);
  }

  public BigDecimal getPendingSettlementAmount(Long sellerId) {
    return settlementRepository.getPendingSettlementAmount(sellerId);
  }

  public Page<FlatSettlementsDTO> findSettlementsByUserId(Long userId, Pageable pageable) {
    return settlementCustomRepository.findSettlementsByUserId(userId, pageable);
  }

  public Page<FlatAdminSettlementsDTO> findAdminSettlementsByUserId(
      Long adminUserId, Pageable pageable) {
    return settlementCustomRepository.findAdminSettlementsByUserId(adminUserId, pageable);
  }

  public Page<FlatPerTransactionSettlement> findPerTransactionSettlementsByIdAndUserId(
      Long userId, Long settlementId, Pageable pageable) {
    return settlementCustomRepository.findPerTransactionSettlementsByIdAndUserId(
        userId, settlementId, pageable);
  }

  public List<Settlement> findAllByUserId(Long userId) {
    return settlementRepository.findAllByUserId(userId);
  }

  /** 정산 항목 존재 여부 확인 */
  public boolean existsSettlementItemByPurchaseId(Long purchaseId) {
    return settlementItemRepository.existsByPurchaseId(purchaseId);
  }

  /** 구매 ID로 정산 항목 조회 */
  public Optional<SettlementItem> findSettlementItemByPurchaseId(Long purchaseId) {
    return settlementItemRepository.findByPurchaseId(purchaseId);
  }

  /** 구매 ID로 정산 항목 조회 (필수) */
  public SettlementItem getSettlementItemByPurchaseId(Long purchaseId) {
    return settlementItemRepository
        .findByPurchaseId(purchaseId)
        .orElseThrow(
            () -> new EntityNotFoundException("정산 항목을 찾을 수 없습니다 - purchaseId: " + purchaseId));
  }
}
