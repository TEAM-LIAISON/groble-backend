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
public class UserMyPageSummaryDto {
  private String nickname;
  private String profileImageUrl;
  private String userTypeName; // BUYER, SELLER
  private boolean canSwitchToSeller; // 판매자 전환 가능 여부
}
