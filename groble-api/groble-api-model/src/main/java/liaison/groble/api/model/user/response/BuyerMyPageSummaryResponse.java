package liaison.groble.api.model.user.response;

import liaison.groble.common.response.EnumResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 구매자용 응답
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "구매자 마이페이지 요약 정보 응답")
public class BuyerMyPageSummaryResponse implements MyPageSummaryResponseBase {
  @Schema(description = "사용자 닉네임", example = "김로블")
  private String nickname;

  @Schema(description = "사용자 프로필 이미지 URL", example = "https://example.com/profile.jpg")
  private String profileImageUrl;

  @Schema(description = "사용자 유형 정보", example = "구매자")
  private EnumResponse userType;

  @Schema(description = "판매자 계정 전환 가능 여부", example = "true")
  private boolean canSwitchToSeller;
}
