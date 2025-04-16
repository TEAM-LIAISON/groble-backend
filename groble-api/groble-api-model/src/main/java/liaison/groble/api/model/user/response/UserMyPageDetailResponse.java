package liaison.groble.api.model.user.response;

import liaison.groble.api.model.user.enums.AccountTypeDto;
import liaison.groble.api.model.user.enums.ProviderTypeDto;

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
  private AccountTypeDto accountType;
  private ProviderTypeDto providerType;
  private String email;
  private String profileImageUrl;
  private String phoneNumber;
  private boolean sellerAccountNotCreated;
}
