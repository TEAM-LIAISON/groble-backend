package liaison.groble.application.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserWithdrawalDto {
  private String reason;
  private String additionalComment;

  public UserWithdrawalDto(String reason, String additionalComment) {
    this.reason = reason;
    this.additionalComment = additionalComment;
  }
}
