package liaison.groble.api.model.content.response;

import java.math.BigDecimal;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "콘텐츠 상세 정보 응답")
public class ContentDetailResponse {
  @Schema(description = "콘텐츠 ID", example = "1")
  private Long contentId;

  // TODO: 콘텐츠에 등록된 모든 이미지들의 URL
  @Schema(
      description = "콘텐츠 이미지 URL 목록",
      example = "[\"https://example.com/image1.jpg\", \"https://example.com/image2.jpg\"]")
  private List<String> contentsImageUrls;

  @Schema(description = "콘텐츠 유형 [COACHING - 코칭], [DOCUMENT - 자료]", example = "COACHING")
  private String contentType;

  @Schema(description = "카테고리 ID", example = "1")
  private Long categoryId;

  @Schema(description = "콘텐츠 이름", example = "사업계획서 컨설팅")
  private String title;

  @Schema(description = "판매자 프로필 이미지 URL")
  private String sellerProfileImageUrl;

  @Schema(description = "판매자 이름")
  private String sellerName;

  @Schema(description = "콘텐츠 최저가", example = "10000")
  private BigDecimal lowestPrice;

  @Schema(description = "콘텐츠 옵션 목록")
  private List<BaseOptionResponse> options;

  @Schema(description = "서비스 타겟", example = "초창패, 창중, 예창패, 청창사 등을 준비하는 분")
  private String serviceTarget;

  @Schema(description = "제공 절차", example = "STANDARD/DELUXE/PREMIUM")
  private String serviceProcess;

  @Schema(description = "메이커 소개", example = "- 동국대학교 철학과 졸업")
  private String makerIntro;
}
