package liaison.groble.api.model.payment.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Client에서 수신한 페이플 인증 결과를 서버에 보내는 Request DTO")
public class PaypleAuthResultRequest {
  @Schema(
      description = "결제 결과 (success OR error OR close)",
      example = "success",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("PCD_PAY_RST")
  private String payRst;

  @Schema(
      description =
          "특정 앱카드 및 간편페이(네이버페이, 카카오페이) 수단을 이용하는 파트너사에 한해 응답되며, 구매자가 선택한 결제수단입니다. 앱카드 : appCard, 네이버페이 : naverPay, 카카오페이 : kakaoPay",
      example = "appCard",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("PCD_PAY_METHOD")
  private String pcdPayMethod;

  @Schema(
      description = "응답 코드",
      example = "0000",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("PCD_PAY_CODE")
  private String payCode;
}
