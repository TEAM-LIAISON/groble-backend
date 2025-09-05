package liaison.groble.application.admin.settlement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 페이플 계좌 인증 요청 DTO
 *
 * <p>페이플 정산지급대행을 위한 계좌 인증 요청 모델입니다.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaypleAccountVerificationRequest {

  private String cstId;

  private String custKey;

  private String bankCodeStd;

  private String accountNum;

  private String accountHolderInfoType;

  private String accountHolderInfo;

  private String subId;
}
