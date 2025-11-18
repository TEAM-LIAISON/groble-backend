package liaison.groble.api.model.payment.request;

import jakarta.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "PaypleBillingRegistrationRequest", description = "Payple 빌링키 등록 요청")
public class PaypleBillingRegistrationRequest {

  @NotBlank
  @Schema(description = "페이플 빌링키 (PCD_PAYER_ID)", example = "PAYER1234567890")
  @JsonProperty("PCD_PAYER_ID")
  private String billingKey;

  @Schema(description = "카드사명", example = "신한카드")
  @JsonProperty("PCD_PAY_CARDNAME")
  private String cardName;

  @Schema(description = "마스킹된 카드번호", example = "1234-****-****-5678")
  @JsonProperty("PCD_PAY_CARDNUM")
  private String cardNumberMasked;
}
