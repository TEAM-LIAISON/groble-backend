package liaison.groble.domain.settlement.repository;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import liaison.groble.domain.settlement.dto.FlatPerTransactionSettlement;
import liaison.groble.domain.settlement.dto.FlatSettlementsDTO;

public interface SettlementCustomRepository {
  Page<FlatSettlementsDTO> findSettlementsByUserId(Long userId, Pageable pageable);

  Page<FlatPerTransactionSettlement> findPerTransactionSettlementsByUserIdAndYearMonth(
      Long userId, LocalDate periodStart, LocalDate periodEnd, Pageable pageable);
}
