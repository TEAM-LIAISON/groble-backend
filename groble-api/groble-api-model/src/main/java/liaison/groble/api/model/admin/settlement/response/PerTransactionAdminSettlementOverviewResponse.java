package liaison.groble.api.model.admin.settlement.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "[✅ 관리자 페이지 - 정산 관리] 정산 상세 정보 - 총 판매내역 내부 판매 정보 응답")
public class PerTransactionAdminSettlementOverviewResponse {
  @Schema(
      description = "상품 제목",
      example = "AI 툴 개발 가이드",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String contentTitle;

  // 정산 금액
  @Schema(
      description = "표시용 정산 금액 (플랫폼/PG 표시 수수료 기준)",
      example = "9980000",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private BigDecimal settlementAmount;

  @Schema(
      description = "주문 상태 [PAID - 결제완료], [CANCEL_REQUEST - 결제취소], [CANCELLED - 환불완료]",
      example = "CANCELLED",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String orderStatus;

  // 판매일
  @Schema(
      description = "판매일 (YYYY-MM-DDTHH:mm:ss)",
      example = "2023-01-15T10:30:00",
      type = "date-time",
      requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime purchasedAt;
}
