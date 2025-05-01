package liaison.groble.application.content.dto;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ContentDetailDto {
  private Long id;
  // 썸네일 이미지 URL
  private String thumbnailUrl;
  // 컨텐츠 타입
  private String contentType;
  // 카테고리 대분류
  private String categoryLarge;
  // 카테고리 소분류
  private String categorySmall;
  // 컨텐츠 제목
  private String title;
  // 판매나 프로필 이미지 URL
  private String sellerProfileImageUrl;
  // 판매자 이름
  private String sellerName;

  // 컨텐츠 옵션 리스트
  private List<ContentOptionDto> options;
}
