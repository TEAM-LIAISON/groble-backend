package liaison.groble.application.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
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
