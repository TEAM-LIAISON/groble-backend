package liaison.groble.application.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserMyPageSummaryDTO {
  private String nickname;
  private String profileImageUrl;
  private String userTypeName;
  private boolean canSwitchToSeller;
  private boolean alreadyRegisteredAsSeller;

  // 인증 상태 관련 필드
  private String verificationStatusName;
  private String verificationStatusDisplayName;
}
