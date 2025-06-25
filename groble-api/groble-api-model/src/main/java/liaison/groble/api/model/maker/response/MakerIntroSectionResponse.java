package liaison.groble.api.model.maker.response;

import liaison.groble.api.model.content.response.ContentPreviewCardResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MakerIntroSectionResponse {
  // 판매자 프로필 이미지 경로
  private String profileImageUrl;

  // 마켓 이름
  private String marketName;

  // 인증 상태
  private String verificationStatus;

  // 판매자의 문의하기 (연락처 수단) 정보
  private ContactInfoResponse contactInfo;

  // 대표 콘텐츠 정보
  private ContentPreviewCardResponse representativeContent;
}
