package liaison.groble.api.model.auth.request;

import jakarta.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenRequest {
  @NotBlank(message = "리프레시 토큰은 필수 입력값입니다.")
  private String refreshToken;
}
