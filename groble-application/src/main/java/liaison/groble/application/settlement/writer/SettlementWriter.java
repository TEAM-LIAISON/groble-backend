package liaison.groble.application.settlement.writer;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.domain.settlement.entity.Settlement;
import liaison.groble.domain.settlement.entity.SettlementItem;
import liaison.groble.domain.settlement.repository.SettlementItemRepository;
import liaison.groble.domain.settlement.repository.SettlementRepository;
import liaison.groble.domain.user.entity.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** 정산 생성 담당 전용 컴포넌트 */
@Slf4j
@Component
@RequiredArgsConstructor
@Transactional
public class SettlementWriter {
  private final SettlementRepository settlementRepository;
  private final SettlementItemRepository settlementItemRepository;

  public Settlement createSettlement(
      User seller,
      LocalDate periodStart,
      LocalDate periodEnd,
      BigDecimal platformFeeRate,
      BigDecimal pgFeeRate) {
    Settlement settlement =
        Settlement.builder()
            .user(seller)
            .settlementStartDate(periodStart)
            .settlementEndDate(periodEnd)
            .platformFeeRate(platformFeeRate) // 플랫폼 수수료율
            .pgFeeRate(pgFeeRate) // PG 수수료율
            .build();
    return settlementRepository.save(settlement);
  }

  /** Settlement 생성 */
  public Settlement createSettlement(User seller, LocalDate periodStart, LocalDate periodEnd) {
    Settlement settlement =
        Settlement.builder()
            .user(seller)
            .settlementStartDate(periodStart)
            .settlementEndDate(periodEnd)
            .platformFeeRate(new BigDecimal("0.0150")) // 1.5%
            .pgFeeRate(new BigDecimal("0.0170")) // 1.7%
            .build();

    return settlementRepository.save(settlement);
  }

  public Settlement saveSettlement(Settlement settlement) {
    return settlementRepository.save(settlement);
  }

  public SettlementItem saveSettlementItem(SettlementItem settlementItem) {
    return settlementItemRepository.save(settlementItem);
  }
}
