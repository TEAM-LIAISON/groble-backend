package liaison.groble.external.hyphen.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class HyphenOneWonTransferResponse {

  @JsonProperty("replyCode")
  private String replyCode; // 응답코드

  @JsonProperty("replyMessage")
  private String replyMessage; // 응답메세지

  @JsonProperty("sign")
  private String sign;

  @JsonProperty("successYn")
  private String successYn; // 성공여부(Y:성공, N:실패, W:타임아웃)

  @JsonProperty("tradeTime")
  private String tradeTime; // 이체시간

  @JsonProperty("inPrintContent")
  private String inPrintContent; // 1원인증용 적요

  @JsonProperty("svcCharge")
  private String svcCharge; // 수수료

  @JsonProperty("oriSeqNo")
  private String oriSeqNo; // 이체결과 조회시 사용되는 값

  @JsonProperty("tr_date")
  private String trDate; // 1원인증 요청 날짜(yyyy-mm-dd)

  @JsonProperty("error")
  private String error; // 에러메세지

  // 성공 여부 확인
  public boolean isSuccess() {
    return "Y".equals(successYn);
  }
}
