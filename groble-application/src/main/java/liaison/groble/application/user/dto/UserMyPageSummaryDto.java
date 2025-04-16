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
  private Long id;
  private String nickName;
  private String profileImageUrl;
  private String userTypeDescription;
  private boolean canSwitchToSeller;
}
