package liaison.groble.application.market.dto;

import java.util.List;

import liaison.groble.application.content.dto.ContentCardDTO;
import liaison.groble.domain.content.dto.FlatContentPreviewDTO;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MarketIntroSectionDTO {
  // 판매자 프로필 이미지 경로
  private String profileImageUrl;

  // 마켓 이름
  private String marketName;

  // groble.im/ 뒤에 붙는 메이커만의 마켓 링크 URL
  private String marketLinkUrl;

  // 인증 상태
  private String verificationStatus;

  // 메이커 연락처 정보
  private ContactInfoDTO contactInfo;

  // 대표 콘텐츠 정보
  private FlatContentPreviewDTO representativeContent;

  private List<ContentCardDTO> contentCardList;
}
