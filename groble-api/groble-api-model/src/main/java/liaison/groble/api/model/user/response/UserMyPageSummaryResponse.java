package liaison.groble.api.model.user.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserMyPageSummaryResponse {
  private String nickName;
  private String profileImageUrl;
  private String userType;
  private boolean canSwitchToSeller;
}
