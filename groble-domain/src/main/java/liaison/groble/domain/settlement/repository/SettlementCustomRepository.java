package liaison.groble.domain.settlement.repository;

import java.math.BigDecimal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import liaison.groble.domain.settlement.dto.FlatAdminSettlementsDTO;
import liaison.groble.domain.settlement.dto.FlatPerTransactionSettlement;
import liaison.groble.domain.settlement.dto.FlatSettlementsDTO;

public interface SettlementCustomRepository {
  Page<FlatSettlementsDTO> findSettlementsByUserId(Long userId, Pageable pageable);

  Page<FlatPerTransactionSettlement> findPerTransactionSettlementsByIdAndUserId(
      Long userId, Long settlementId, Pageable pageable);

  Page<FlatPerTransactionSettlement> findSalesListBySettlementId(
      Long settlementId, Pageable pageable);

  Page<FlatAdminSettlementsDTO> findAdminSettlementsByUserId(Long adminUserId, Pageable pageable);

  BigDecimal getTotalCompletedPlatformFee();
}
