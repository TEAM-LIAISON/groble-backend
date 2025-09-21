package liaison.groble.api.model.admin.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "관리자 계좌 인증 결과 응답")
public class AdminAccountVerificationResponse {

  @Schema(description = "계좌 인증 성공 여부", example = "true")
  private boolean success;

  @Schema(description = "페이플 결과 코드", example = "A0000")
  private String resultCode;

  @Schema(description = "페이플 결과 메시지", example = "정상 처리되었습니다.")
  private String message;

  @Schema(description = "페이플 빌링 거래 ID", example = "billing1234")
  private String billingTranId;

  @Schema(description = "페이플 API 거래 일시", example = "2025-09-11 12:30:45")
  private String apiTranDtm;

  @Schema(description = "은행 거래 ID", example = "T99887766")
  private String bankTranId;

  @Schema(description = "은행 거래 일자", example = "20250911")
  private String bankTranDate;

  @Schema(description = "은행 응답 코드", example = "00000000")
  private String bankRspCode;

  @Schema(description = "은행 표준 코드", example = "004")
  private String bankCodeStd;

  @Schema(description = "은행 서브 코드", example = "001")
  private String bankCodeSub;
}
