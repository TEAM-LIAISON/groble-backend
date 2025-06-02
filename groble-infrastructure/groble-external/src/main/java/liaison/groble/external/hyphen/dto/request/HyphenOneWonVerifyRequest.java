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
public class HyphenOneWonVerifyRequest {

  @JsonProperty("oriSeqNo")
  private String oriSeqNo; // 인증 검증 번호(1 Tr에서 얻은 값)

  @JsonProperty("inPrintContent")
  private String inPrintContent; // 인증 고객 입력 적요 (ex 파란하늘) *5분이내로 입력

  @JsonProperty("tr_date")
  private String trDate; // 거래날짜(yyyy-mm-dd)
}
