package liaison.groble.application.content.dto;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ContentDetailDto {
  // 콘텐츠 ID
  private Long contentId;
  // 썸네일 이미지 URL
  private String thumbnailUrl;
  // 콘텐츠 타입
  private String contentType;
  // 카테고리 ID
  private Long categoryId;
  // 콘텐츠 제목
  private String title;
  // 판매나 프로필 이미지 URL
  private String sellerProfileImageUrl;
  // 판매자 이름
  private String sellerName;

  // 콘텐츠 옵션 리스트
  private List<ContentOptionDto> options;
}
