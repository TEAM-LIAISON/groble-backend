package liaison.groble.application.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserMyPageSummaryDto {
  private String nickname;
  private String profileImageUrl;
  private String userTypeName;
  private boolean canSwitchToSeller;

  // 인증 상태 관련 필드
  private String verificationStatusName;
  private String verificationStatusDisplayName;

  // 구매자 관련 필드
  private Integer orderCount;

  // 판매자 관련 필드
  private Integer pendingShipmentCount;
  private Long pendingSettlementAmount;
}
