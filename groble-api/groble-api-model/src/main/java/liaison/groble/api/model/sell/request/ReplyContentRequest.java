package liaison.groble.api.model.sell.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReplyContentRequest {
  @Schema(
      description = "답글 내용",
      example = "리뷰에 대한 답글이에요",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String replyContent;
}
