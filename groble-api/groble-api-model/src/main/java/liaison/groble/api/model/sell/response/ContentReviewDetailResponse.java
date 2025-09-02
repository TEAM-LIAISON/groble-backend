package liaison.groble.api.model.sell.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "[내 스토어 - 상품 관리 - 판매 관리- 리뷰 상세] 정보 응답")
public class ContentReviewDetailResponse {
  @Schema(
      description = "콘텐츠 리뷰 ID",
      example = "100",
      type = "integer",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private Long reviewId;

  @Schema(
      description = "콘텐츠 리뷰 상태 (ACTIVE - 삭제 요청 가능, PENDING_DELETE - 삭제 요청됨)",
      example = "ACTIVE",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String reviewStatus;

  @Schema(
      description = "콘텐츠 제목",
      example = "자바 프로그래밍 코칭",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String contentTitle;

  @Schema(description = "리뷰 작성 일시", example = "2025-04-20T10:15:30")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime createdAt;

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
