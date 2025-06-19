package liaison.groble.api.model.user.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserHeaderResponse {
  @Schema(description = "로그인 상태 여부", example = "true")
  private Boolean isLogin;

  @Schema(description = "최종 로그인 상태 여부", example = "true")
  private Boolean isLoginCompleted;

  @Schema(description = "사용자 닉네임", example = "홍길동")
  private String nickname;

  @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
  private String profileImageUrl;

  @Schema(description = "판매자 전환 가능 여부 [현재 BUYER라면 true, SELLER라면 false]", example = "false")
  private boolean canSwitchToSeller;

  @Schema(description = "읽지 않은 알림 개수", example = "5")
  private long unreadNotificationCount;

  @Schema(description = "판매자 등록 여부 [사용자의 SELLER 소유 여부 판단]", example = "false")
  private boolean alreadyRegisteredAsSeller;

  @Schema(description = "마지막으로 사용한 사용자 유형", example = "SELLER")
  private String lastUserType;
}
