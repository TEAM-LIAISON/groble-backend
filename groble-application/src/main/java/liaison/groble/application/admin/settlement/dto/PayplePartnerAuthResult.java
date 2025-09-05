package liaison.groble.application.admin.settlement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 페이플 파트너 인증 결과 DTO
 *
 * <p>페이플 정산지급대행을 위한 파트너 인증 응답을 담는 모델입니다.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayplePartnerAuthResult {

  private String result;

  private String message;

  private String code;

  private String accessToken;

  private String tokenType;

  private String expiresIn;

  /**
   * 인증 성공 여부 확인
   *
   * @return 성공 시 true
   */
  public boolean isSuccess() {
    return "T0000".equals(result);
  }
}
