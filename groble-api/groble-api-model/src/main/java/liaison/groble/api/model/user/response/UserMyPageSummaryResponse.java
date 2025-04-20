package liaison.groble.api.model.user.response;

import liaison.groble.common.response.EnumResponse;

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
  private EnumResponse userType;
  private boolean canSwitchToSeller;
}
