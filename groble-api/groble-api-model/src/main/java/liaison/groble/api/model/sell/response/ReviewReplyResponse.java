package liaison.groble.api.model.sell.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "리뷰 답글 응답")
public class ReviewReplyResponse {
  @Schema(
      description = "콘텐츠 리뷰 답글 ID",
      example = "150",
      type = "integer",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private Long replyId;

  @Schema(description = "리뷰 답글 작성 일시", example = "2025-04-20T10:15:30")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime createdAt;

  @Schema(
      description = "답글 작성자 닉네임",
      example = "김그로블",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String replierNickname;

  @Schema(
      description = "답글 작성 내용",
      example = "감사합니다",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String replyContent;
}
