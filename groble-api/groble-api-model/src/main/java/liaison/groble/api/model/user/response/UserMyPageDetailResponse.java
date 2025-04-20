package liaison.groble.api.model.user.response;

import liaison.groble.common.response.EnumResponse;

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
  private String nickName;

  @Schema(
      description = "사용자 계정 정보 객체 (INTEGRATED - 통합 계정, SOCIAL - 소셜 계정)",
      example = "{ " + "\"code\": \"INTEGRATED\", " + "\"description\": \"통합 계정\" }")
  private EnumResponse accountType;

  @Schema(
      description = "소셜 계정에 대한 플랫폼 정보",
      example = "{ " + "\"code\": \"KAKAO\", " + "\"description\": \"카카오\" }")
  private EnumResponse providerType;

  @Schema(description = "이메일", example = "kwondm7@naver.com")
  private String email;

  @Schema(description = "사용자 프로필 이미지 URL", example = "https://example.com/profile.jpg")
  private String profileImageUrl;

  @Schema(description = "사용자 전화번호", example = "010-1234-5678")
  private String phoneNumber;

  @Schema(description = "판매자 계정 미생성 여부", example = "true")
  private boolean sellerAccountNotCreated;
}
