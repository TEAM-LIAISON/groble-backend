package liaison.groble.external.hyphen.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HyphenTransferRequest {
  private String WD_ACCT_NO;
  private String WD_BANK_CODE;
  private String RCV_ACCT_NO;
  private String RCV_BANK_CODE;
  private String TR_AMT;
  private String RCV_NM;
  private String TR_MSG;
}
