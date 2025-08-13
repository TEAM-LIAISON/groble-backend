package liaison.groble.api.model.settlement.response;

import java.math.BigDecimal;
import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "[✅ 내 스토어 - 정산관리 - 정산 상세 - 정산 상세 내역] 정산 상세 내역 응답")
public class SettlementDetailResponse {
  // 정산 시작일 (년도 + 월) - Settlement class
  private LocalDate settlementStartDate;

  // 정산 종료일 (년도 + 월) - Settlement class
  private LocalDate settlementEndDate;
  // 정산(예정)일
  private LocalDate scheduledSettlementDate;
  // 정산(예정)금액
  private BigDecimal settlementAmount; // 정산 예정 금액 (원화 표기)
  // PG사 수수료(1.7%)
  private BigDecimal pgFee; // PG사 수수료 (1.7%)
  // 그로블 수수료(1.5%)
  private BigDecimal platformFee; // 플랫폼 수수료 (1.5%)
}
