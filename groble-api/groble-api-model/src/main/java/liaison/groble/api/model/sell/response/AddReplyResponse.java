package liaison.groble.api.model.sell.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "[내 스토어 - 상품 관리 - 리뷰 내역 상세 - 리뷰 답글 달기] 정보 응답")
public class AddReplyResponse {
  @Schema(
      description = "답글 내용",
      example = "리뷰에 대한 답글이에요",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String replyContent;
}
