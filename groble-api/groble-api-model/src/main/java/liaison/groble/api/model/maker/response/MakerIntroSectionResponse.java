package liaison.groble.api.model.maker.response;

import liaison.groble.api.model.content.response.ContentPreviewCardResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MakerIntroSectionResponse {

  @Schema(
      description = "프로필 이미지 경로",
      example = "https://example.com/profile.jpg",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String profileImageUrl;

  @Schema(
      description = "마켓 이름",
      example = "동민님의 마켓",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String marketName;

  @Schema(
      description =
          "메이커 인증 상태 (PENDING - 인증 필요, IN_PROGRESS - 인증 대기 중, FAILED - 인증 실패, VERIFIED - 인증 완료)",
      example = "VERIFIED",
      type = "string",
      allowableValues = {"PENDING", "IN_PROGRESS", "FAILED", "VERIFIED"},
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String verificationStatus;

  // 판매자의 문의하기 (연락처 수단) 정보
  @Schema(
      description = "판매자 문의처 정보",
      implementation = ContactInfoResponse.class,
      requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  private ContactInfoResponse contactInfo;

  // 대표 콘텐츠 정보
  @Schema(
      description = "대표 콘텐츠 정보 (null 일 수 있음)",
      implementation = ContentPreviewCardResponse.class,
      requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  private ContentPreviewCardResponse representativeContent;
}
