package liaison.groble.api.model.payment.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PaypleLinkResendRequest {

  @NotNull(message = "전송 방법은 필수입니다.")
  @Pattern(regexp = "^(SMS|EMAIL)$", message = "전송 방법은 SMS 또는 EMAIL만 가능합니다.")
  private String method;

  private String targetPhoneNumber; // SMS 전송 시 대상 번호 (선택)
  private String targetEmail; // EMAIL 전송 시 대상 이메일 (선택)
}
