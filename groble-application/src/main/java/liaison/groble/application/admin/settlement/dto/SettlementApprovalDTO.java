package liaison.groble.application.admin.settlement.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 정산 승인 결과 DTO (Application Layer)
 *
 * <p>AdminSettlementService에서 반환하는 정산 승인 처리 결과입니다.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettlementApprovalDTO {

  private boolean success;
  private int approvedSettlementCount;
  private int approvedItemCount;
  private BigDecimal totalApprovedAmount;
  private LocalDateTime approvedAt;
  private PaypleSettlementResultDTO paypleResult;
  private List<FailedSettlementDTO> failedSettlements;
  private int excludedRefundedItemCount;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class PaypleSettlementResultDTO {
    private boolean success;
    private String responseCode;
    private String responseMessage;
    private String accessToken;
    private String expiresIn;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class FailedSettlementDTO {
    private Long settlementId;
    private String failureReason;
  }
}
