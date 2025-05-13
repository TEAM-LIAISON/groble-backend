package liaison.groble.api.model.user.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserHeaderResponse {
  private Boolean isLogin;
  private String nickname;
  private String profileImageUrl;
  private boolean canSwitchToSeller;
  private long unreadNotificationCount;
}
