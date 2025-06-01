package liaison.groble.external.hyphen.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HyphenTransferRequest {
  private String WD_ACCT_NO;
  private String WD_BANK_CODE;
  private String RCV_ACCT_NO;
  private String RCV_BANK_CODE;
  private String TR_AMT;
  private String RCV_NM;
  private String TR_MSG;
}
