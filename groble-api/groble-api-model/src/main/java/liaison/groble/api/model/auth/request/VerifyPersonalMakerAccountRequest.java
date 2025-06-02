package liaison.groble.api.model.auth.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "개인 메이커 인증 처리 요청")
public class VerifyPersonalMakerAccountRequest {
  /** 정산받을 은행 계좌 소유자 이름 */
  private String bankAccountOwner;

  /** 정산받을 은행명 */
  private String bankName;

  /** 정산받을 계좌번호 */
  private String bankAccountNumber;

  /** 통장 사본 첨부 URL */
  private String copyOfBankbookUrl;
}
