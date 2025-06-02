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
public class HyphenOneWonTransferRequest {

  @JsonProperty("inBankCode")
  private String inBankCode;

  @JsonProperty("inAccount")
  private String inAccount;

  @Builder.Default
  @JsonProperty("msgType")
  private String msgType = "1"; // 기본값 지정

  @JsonProperty("compName")
  private String compName;

  @JsonProperty("printContent")
  private String printContent;
}
