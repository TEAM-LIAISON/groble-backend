package liaison.groble.application.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SignUpAuthResultDTO {
  private String email;
  private String accessToken; // 엑세스 토큰
  private String refreshToken; // 리프레시 토큰
}
