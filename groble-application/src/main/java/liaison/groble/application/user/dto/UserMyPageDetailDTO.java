package liaison.groble.application.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserMyPageDetailDTO {
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
