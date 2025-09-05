package liaison.groble.application.guest.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UpdateGuestUserInfoResultDTO {
  private String email;
  private String username;
  private String newGuestToken; // 업그레이드된 토큰
}
