package liaison.groble.domain.settlement.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import liaison.groble.domain.settlement.dto.FlatMonthlySettlement;

public interface SettlementCustomRepository {
  Page<FlatMonthlySettlement> findMonthlySettlementsByUserId(Long userId, Pageable pageable);
}
