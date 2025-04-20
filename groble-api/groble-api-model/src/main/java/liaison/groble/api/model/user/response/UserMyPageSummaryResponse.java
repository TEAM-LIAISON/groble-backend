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
@Schema(description = "마이페이지 요약 정보 응답")
public class UserMyPageSummaryResponse {

  @Schema(description = "사용자 닉네임", example = "권동민")
  private String nickname;

  @Schema(description = "사용자 프로필 이미지 URL", example = "https://example.com/profile.jpg")
  private String profileImageUrl;

  @Schema(description = "사용자 유형 정보 객체 (BUYER - 구매자, SELLER - 판매자)")
  private UserTypeResponse userType;

  @Schema(description = "판매자 계정 전환 가능 여부", example = "true")
  private boolean canSwitchToSeller;
}
