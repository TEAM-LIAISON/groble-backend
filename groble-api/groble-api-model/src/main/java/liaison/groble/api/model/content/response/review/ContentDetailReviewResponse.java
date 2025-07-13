package liaison.groble.api.model.content.response.review;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import liaison.groble.api.model.sell.response.ReviewReplyResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "콘텐츠 상세 리뷰 응답")
public class ContentDetailReviewResponse {
  @Schema(
      description = "콘텐츠 리뷰 ID",
      example = "100",
      type = "integer",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private Long reviewId;

  @Schema(description = "리뷰 작성 일시", example = "2025-04-20T10:15:30")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime createdAt;

  @Schema(
      description = "리뷰 작성자 프로필 이미지 URL",
      example = "https://example.com/profile.jpg",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String reviewerProfileImageUrl;

  @Schema(
      description = "리뷰 작성자 닉네임",
      example = "뚜비뚜비",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String reviewerNickname;

  @Schema(
      description = "리뷰 작성 내용",
      example = "마음에 들어요",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String reviewContent;

  @Schema(
      description = "구매한 콘텐츠 옵션 이름",
      example = "옵션 이름",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String selectedOptionName;

  @Schema(
      description = "리뷰 별점",
      example = "4.5",
      type = "number",
      format = "decimal",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private BigDecimal rating;

  @Schema(
      description = "리뷰에 대한 판매자 답글",
      implementation = ReviewReplyResponse.class,
      requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  private List<ReviewReplyResponse> reviewReplies;
}
