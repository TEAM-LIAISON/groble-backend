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

  @Schema(description = "사용자 닉네임", example = "홍길동")
  private String nickname;

  @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
  private String profileImageUrl;

  @Schema(description = "판매자 전환 가능 여부", example = "false")
  private boolean canSwitchToSeller;

  @Schema(description = "읽지 않은 알림 개수", example = "5")
  private long unreadNotificationCount;

  @Schema(description = "판매자 등록 여부", example = "false")
  private boolean alreadyRegisteredAsSeller;
}
