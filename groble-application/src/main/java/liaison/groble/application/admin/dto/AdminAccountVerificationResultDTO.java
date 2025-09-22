package liaison.groble.application.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 관리자 계좌 인증 결과 DTO */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminAccountVerificationResultDTO {

  private boolean success;
  private String resultCode;
  private String message;
  private String billingTranId;
  private String apiTranDtm;
  private String bankTranId;
  private String bankTranDate;
  private String bankRspCode;
  private String bankCodeStd;
  private String bankCodeSub;
}
