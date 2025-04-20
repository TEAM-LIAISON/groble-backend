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
public class UserMyPageDetailResponse {
  private String nickName;
  private EnumResponse accountType;
  private EnumResponse providerType;
  private String email;
  private String profileImageUrl;
  private String phoneNumber;
  private boolean sellerAccountNotCreated;
}
