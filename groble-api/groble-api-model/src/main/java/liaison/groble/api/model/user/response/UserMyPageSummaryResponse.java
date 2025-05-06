package liaison.groble.api.model.user.response;

import liaison.groble.common.response.EnumResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

// 4. 현재 타입 래퍼 클래스 (기존 코드와의 호환성 유지)
@Getter
@Schema(description = "마이페이지 요약 정보 응답")
public class UserMyPageSummaryResponse {
  // data 필드를 제거하고 MyPageSummaryResponseBase의 모든 필드를 직접 위임
  @Schema(description = "사용자 닉네임", example = "김로블")
  private final String nickname;

  @Schema(description = "프로필 이미지 URL")
  private final String profileImageUrl;

  @Schema(description = "사용자 유형", implementation = EnumResponse.class)
  private final EnumResponse userType;

  // BuyerMyPageSummaryResponse에만 있는 필드
  @Schema(description = "판매자 전환 가능 여부", example = "false")
  private final Boolean canSwitchToSeller;

  // SellerMyPageSummaryResponse에만 있는 필드
  @Schema(description = "판매자 인증 상태")
  private final EnumResponse verificationStatus;

  // 생성자 - 구매자용
  public UserMyPageSummaryResponse(BuyerMyPageSummaryResponse response) {
    this.nickname = response.getNickname();
    this.profileImageUrl = response.getProfileImageUrl();
    this.userType = response.getUserType();
    this.canSwitchToSeller = response.isCanSwitchToSeller();
    this.verificationStatus = null;
  }

  // 생성자 - 판매자용
  public UserMyPageSummaryResponse(SellerMyPageSummaryResponse response) {
    this.nickname = response.getNickname();
    this.profileImageUrl = response.getProfileImageUrl();
    this.userType = response.getUserType();
    this.canSwitchToSeller = null;
    this.verificationStatus = response.getVerificationStatus();
  }
}
