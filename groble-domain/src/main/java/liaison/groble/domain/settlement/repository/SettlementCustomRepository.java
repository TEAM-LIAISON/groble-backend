package liaison.groble.domain.settlement.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import liaison.groble.domain.settlement.dto.FlatPerTransactionSettlement;
import liaison.groble.domain.settlement.dto.FlatSettlementsDTO;
import liaison.groble.domain.settlement.entity.Settlement;

public interface SettlementCustomRepository {
  Page<FlatSettlementsDTO> findSettlementsByUserId(Long userId, Pageable pageable);

  Page<FlatPerTransactionSettlement> findPerTransactionSettlementsByIdAndUserId(
      Long userId, Long settlementId, Pageable pageable);

  /**
   * 모든 사용자의 정산 내역 조회 (관리자용)
   *
   * @param pageable 페이징 정보
   * @return 페이징된 정산 목록
   */
  Page<Settlement> findAllUsersSettlementsForAdmin(Pageable pageable);
}
