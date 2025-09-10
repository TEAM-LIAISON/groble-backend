package liaison.groble.application.settlement.writer;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.domain.settlement.entity.Settlement;
import liaison.groble.domain.settlement.entity.SettlementItem;
import liaison.groble.domain.settlement.repository.SettlementItemRepository;
import liaison.groble.domain.settlement.repository.SettlementRepository;

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

  public Settlement saveSettlement(Settlement settlement) {
    return settlementRepository.save(settlement);
  }

  public SettlementItem saveSettlementItem(SettlementItem settlementItem) {
    return settlementItemRepository.save(settlementItem);
  }
}
