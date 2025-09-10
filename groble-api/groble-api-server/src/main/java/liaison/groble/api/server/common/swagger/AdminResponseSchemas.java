package liaison.groble.api.server.common.swagger;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import liaison.groble.api.model.admin.settlement.response.AdminSettlementDetailResponse;
import liaison.groble.api.model.admin.settlement.response.AdminSettlementsOverviewResponse;
import liaison.groble.api.model.admin.settlement.response.PerTransactionAdminSettlementOverviewResponse;
import liaison.groble.api.model.admin.settlement.response.SettlementApprovalResponse;
import liaison.groble.common.response.GrobleResponse;
import liaison.groble.common.response.PageResponse;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 관리자 기능 관련 API 응답 스키마 클래스
 *
 * <p>Swagger 문서화를 위한 관리자 전용 응답 스키마들을 정의합니다.
 */
public final class AdminResponseSchemas {

  private AdminResponseSchemas() {}

  /** 관리자 정산 내역 조회 응답 스키마 */
  @Schema(description = "관리자 정산 내역 조회 응답")
  public static class ApiAdminSettlementsOverviewResponse
      extends GrobleResponse<PageResponse<AdminSettlementsOverviewResponse>> {

    @Override
    @Schema(description = "페이징된 관리자 정산 내역 목록", implementation = PageResponse.class)
    public PageResponse<AdminSettlementsOverviewResponse> getData() {
      return super.getData();
    }
  }

  /** 관리자 정산 상세 조회 응답 스키마 */
  @Schema(description = "관리자 정산 상세 조회 응답")
  public static class ApiAdminSettlementDetailResponse
      extends GrobleResponse<AdminSettlementDetailResponse> {

    @Override
    @Schema(description = "관리자 정산 상세 정보", implementation = AdminSettlementDetailResponse.class)
    public AdminSettlementDetailResponse getData() {
      return super.getData();
    }
  }

  /** 관리자 정산 판매 목록 조회 응답 스키마 */
  @Schema(description = "관리자 정산 판매 목록 조회 응답")
  public static class ApiAdminSettlementSalesListResponse
      extends GrobleResponse<PageResponse<PerTransactionAdminSettlementOverviewResponse>> {

    @Override
    @Schema(description = "페이징된 정산 판매 목록", implementation = PageResponse.class)
    public PageResponse<PerTransactionAdminSettlementOverviewResponse> getData() {
      return super.getData();
    }
  }

  /** 정산 승인 응답 스키마 */
  @Schema(description = "정산 승인 응답")
  public static class ApiSettlementApprovalResponse
      extends GrobleResponse<SettlementApprovalResponse> {

    @Override
    @Schema(description = "정산 승인 결과", implementation = SettlementApprovalResponse.class)
    public SettlementApprovalResponse getData() {
      return super.getData();
    }
  }

  /** 페이플 정산 결과 스키마 */
  @Schema(description = "페이플 정산 처리 결과")
  public static class PaypleSettlementResult {

    @Schema(description = "페이플 처리 성공 여부", example = "true")
    private Boolean success;

    @Schema(description = "페이플 응답 코드", example = "SUCCESS")
    private String responseCode;

    @Schema(description = "페이플 응답 메시지", example = "페이플 정산 승인 및 실행 완료")
    private String responseMessage;

    @Schema(description = "페이플 액세스 토큰", example = "AUTH_TOKEN_12345")
    private String accessToken;

    @Schema(description = "토큰 만료 시간 (초)", example = "3600")
    private String expiresIn;
  }

  /** 정산 실패 항목 스키마 */
  @Schema(description = "정산 실패 항목 정보")
  public static class FailedSettlementInfo {

    @Schema(description = "실패한 정산 ID", example = "156")
    private Long settlementId;

    @Schema(description = "실패 사유", example = "이미 완료된 정산입니다")
    private String failureReason;
  }

  /** 정산 승인 응답 데이터 스키마 */
  @Schema(description = "정산 승인 응답 데이터")
  public static class SettlementApprovalResponseData {

    @Schema(description = "전체 처리 성공 여부", example = "true")
    private Boolean success;

    @Schema(description = "승인된 정산 수", example = "2")
    private Integer approvedSettlementCount;

    @Schema(description = "승인된 정산 항목 수", example = "3")
    private Integer approvedItemCount;

    @Schema(description = "총 승인 금액", example = "285000.00")
    private BigDecimal totalApprovedAmount;

    @Schema(description = "승인 처리 시간", example = "2025-09-09T15:30:45")
    private LocalDateTime approvedAt;

    @Schema(description = "페이플 처리 결과")
    private PaypleSettlementResult paypleResult;

    @Schema(description = "처리 실패한 정산 목록")
    private List<FailedSettlementInfo> failedSettlements;

    @Schema(description = "환불로 인해 제외된 항목 수", example = "1")
    private Integer excludedRefundedItemCount;
  }
}
