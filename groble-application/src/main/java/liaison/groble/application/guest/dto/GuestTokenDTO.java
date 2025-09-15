package liaison.groble.application.guest.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GuestTokenDTO {
  private String phoneNumber;
  private String email;
  private String username;
  private String guestToken;
  private boolean authenticated;
  private boolean hasCompleteUserInfo;
  private boolean buyerInfoStorageAgreed;

  /** 레거시 정보는 있으나 동의가 없어 재동의가 필요한지 */
  private boolean needsBuyerInfoConsent;
}
