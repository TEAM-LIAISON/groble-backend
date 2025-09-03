package liaison.groble.application.admin.settlement.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 정산 승인 요청 DTO (Application Layer)
 *
 * <p>AdminSettlementService에서 사용하는 정산 승인 요청 정보입니다.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettlementApprovalRequestDTO {

  private List<Long> settlementIds;
  private Long adminUserId;
  private String approvalReason;
  private boolean executePaypleSettlement;
}
