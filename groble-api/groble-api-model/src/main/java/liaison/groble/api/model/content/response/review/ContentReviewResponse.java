package liaison.groble.api.model.content.response.review;

import java.math.BigDecimal;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "콘텐츠 리뷰 응답")
public class ContentReviewResponse {
  @Schema(description = "리뷰 평균 별점", example = "4.3", type = "number", format = "decimal")
  private BigDecimal averageRating;

  @Schema(
      description = "전체 리뷰 개수",
      example = "125",
      type = "integer",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private Long totalReviewCount;

  @Schema(description = "리뷰 목록", implementation = ContentDetailReviewResponse.class)
  private List<ContentDetailReviewResponse> reviews;
}
