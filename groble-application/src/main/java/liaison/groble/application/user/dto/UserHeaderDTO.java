package liaison.groble.application.user.dto;

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
public class UserHeaderDTO {
  private Boolean isLogin;
  private String nickname;
  private String email;
  private String profileImageUrl;
  private boolean canSwitchToSeller;
  private long unreadNotificationCount;
  private boolean alreadyRegisteredAsSeller;
  private String lastUserType;
}
