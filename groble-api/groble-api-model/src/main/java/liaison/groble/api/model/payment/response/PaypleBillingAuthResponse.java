package liaison.groble.api.model.payment.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "PaypleBillingAuthResponse", description = "페이플 빌링 파트너 인증 응답")
public class PaypleBillingAuthResponse {

  @Schema(description = "인증 결과 (success/error)", example = "success")
  private String result;

  @Schema(description = "인증 결과 메시지", example = "인증이 완료되었습니다.")
  private String resultMsg;

  @Schema(description = "파트너 ID", example = "groble_cst_0001")
  private String cstId;

  @Schema(description = "파트너 키", example = "custKey123456")
  private String custKey;

  @Schema(description = "인증 키", example = "authKey7890")
  private String authKey;

  @Schema(description = "결제 작업 구분", example = "LINKREG")
  private String payWork;

  @Schema(description = "결제 페이지 URL (MO 방식)", example = "https://payple.kr/billing/mo")
  private String payUrl;

  @Schema(description = "결제 완료 후 리다이렉트 URL", example = "https://groble.im/payment/complete")
  private String returnUrl;
}
