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
public class UserMyPageDetailDto {
  private String nickname;
  private String userTypeName;
  private String accountTypeName;
  private String providerTypeName;
  private String email;
  private String profileImageUrl;
  private String phoneNumber;
  private boolean canSwitchToSeller;
  private boolean sellerAccountNotCreated;
  private String verificationStatus;
  private boolean alreadyRegisteredAsSeller;
}
