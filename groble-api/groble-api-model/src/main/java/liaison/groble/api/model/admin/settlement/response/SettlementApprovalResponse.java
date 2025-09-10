package liaison.groble.api.model.admin.settlement.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 관리자 정산 승인 응답 DTO
 *
 * <p>정산 승인 처리 결과를 반환하는 응답 모델입니다.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "정산 승인 응답")
public class SettlementApprovalResponse {

  @Schema(description = "승인 처리 성공 여부", example = "true")
  private boolean success;

  @Schema(description = "승인 처리된 정산 수", example = "5")
  private int approvedSettlementCount;

  @Schema(description = "승인 처리된 정산 항목 수", example = "15")
  private int approvedItemCount;

  @Schema(description = "총 승인 정산 금액", example = "1500000")
  private BigDecimal totalApprovedAmount;

  @Schema(description = "승인 처리 시각")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime approvedAt;

  @Schema(description = "페이플 정산 요청 결과")
  private PaypleSettlementResult paypleResult;

  @Schema(description = "실패한 정산 목록 (부분 실패 시)")
  private List<FailedSettlement> failedSettlements;

  @Schema(description = "환불로 인해 제외된 정산 항목 수", example = "2")
  private int excludedRefundedItemCount;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(description = "페이플 정산 요청 결과")
  public static class PaypleSettlementResult {

    @Schema(description = "페이플 정산 성공 여부", example = "true")
    private boolean success;

    @Schema(description = "페이플 응답 코드", example = "T0000")
    private String responseCode;

    @Schema(description = "페이플 응답 메시지", example = "처리 성공")
    private String responseMessage;

    @Schema(description = "페이플 Access Token")
    private String accessToken;

    @Schema(description = "토큰 만료 시간(초)", example = "60")
    private String expiresIn;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(description = "실패한 정산")
  public static class FailedSettlement {

    @Schema(description = "정산 ID", example = "123")
    private Long settlementId;

    @Schema(description = "실패 사유", example = "이미 승인된 정산입니다")
    private String failureReason;
  }
}
