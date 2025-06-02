package liaison.groble.external.hyphen.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HyphenAccountVerificationRequest {

  @JsonProperty("bank_cd")
  private String bankCd; // 은행 코드 (3자리)

  @JsonProperty("acct_no")
  private String acctNo; // 계좌번호

  @JsonProperty("id_no")
  private String idNo; // 신원확인번호(생년월일6자리 or 사업자번호)

  @JsonProperty("amount")
  private String amount; // 금액(가상계좌 조회시 사용)
}
