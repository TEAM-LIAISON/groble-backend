package liaison.groble.api.model.content.response;

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
  @Schema(description = "상품 ID", example = "1")
  private Long id;

  @Schema(description = "콘텐츠 유형 [COACHING - 코칭], [DOCUMENT - 자료]", example = "COACHING")
  private String contentType;

  @Schema(description = "카테고리 ID", example = "1")
  private Long categoryId;

  @Schema(description = "카테고리 대분류 이름", example = "AI")
  private String categoryLargeName;

  @Schema(description = "카테고리 소분류 이름", example = "프로세스")
  private String categorySmallName;

  @Schema(description = "컨텐츠 이름", example = "사업계획서 컨설팅")
  private String title;

  @Schema(description = "판매자 프로필 이미지 URL")
  private String sellerProfileImageUrl;

  @Schema(description = "판매자 이름")
  private String sellerName;

  @Schema(description = "상품 옵션 목록")
  private List<ContentOptionResponse> options;
}
