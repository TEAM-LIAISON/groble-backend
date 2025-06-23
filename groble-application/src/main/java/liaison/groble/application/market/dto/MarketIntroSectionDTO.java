package liaison.groble.application.market.dto;

import liaison.groble.application.content.dto.ContentCardDTO;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MarketIntroSectionDTO {
  // 판매자 프로필 이미지 경로
  private String profileImageUrl;

  // 마켓 이름
  private String marketName;

  // 인증 상태
  private String verificationStatus;

  // 메이커 연락처 정보
  private ContactInfoDTO contactInfo;

  // 대표 콘텐츠 정보
  private ContentCardDTO representativeContent;
}
