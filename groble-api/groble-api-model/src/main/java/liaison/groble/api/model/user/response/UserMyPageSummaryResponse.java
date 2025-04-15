package liaison.groble.api.model.user.response;

import liaison.groble.api.model.user.enums.UserTypeDto;

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
  private UserTypeDto userType;
}
