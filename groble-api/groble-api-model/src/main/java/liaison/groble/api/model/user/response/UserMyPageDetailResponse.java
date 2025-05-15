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
public class UserMyPageDetailResponse {
  @Schema(description = "사용자 닉네임", example = "권동민")
  private String nickname;

  @Schema(description = "사용자 마지막 선택 유형", example = "BUYER / SELLER")
  private String userType;

  @Schema(description = "사용자 계정 유형 (INTEGRATED: 통합 계정, SOCIAL: 소셜 계정)")
  private String accountType;

  @Schema(description = "소셜 플랫폼 유형 (가능한 값: KAKAO - 카카오, NAVER - 네이버, GOOGLE - 구글)")
  private String providerType;

  @Schema(description = "이메일", example = "kwondm7@naver.com")
  private String email;

  @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
  private String profileImageUrl;

  @Schema(description = "전화번호", example = "010-1234-5678")
  private String phoneNumber;

  @Schema(description = "판매자 계정 미생성 여부", example = "true")
  private boolean sellerAccountNotCreated;
}
