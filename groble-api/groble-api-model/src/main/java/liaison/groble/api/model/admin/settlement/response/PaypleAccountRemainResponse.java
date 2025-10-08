package liaison.groble.api.model.admin.settlement.response;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "페이플 이체 가능 잔액 조회 응답")
public class PaypleAccountRemainResponse {

  @Schema(description = "응답 코드", example = "A0000")
  private String result;

  @Schema(description = "응답 메시지", example = "정상 처리되었습니다.")
  private String message;

  @Schema(description = "세부 코드", example = "SUCCESS")
  private String code;

  @Schema(description = "누적 정산금액", example = "1000000")
  private BigDecimal totalAccountAmount;

  @Schema(description = "누적 이체 금액", example = "200000")
  private BigDecimal totalTransferAmount;

  @Schema(description = "이체 가능 잔액", example = "800000")
  private BigDecimal remainAmount;

  @Schema(description = "API 요청 타임스탬프", example = "20231022152040289")
  private String apiTranDtm;

  @Schema(description = "누적 정산 차액 (PG 수수료 환급 예상액 합계)", example = "120000")
  private BigDecimal cumulativePgFeeRefundExpected;
}
