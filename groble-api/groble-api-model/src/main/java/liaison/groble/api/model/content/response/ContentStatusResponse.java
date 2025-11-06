package liaison.groble.api.model.content.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ContentStatusResponse {
  @Schema(
      description = "콘텐츠 ID",
      example = "50",
      type = "integer",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private Long contentId;

  @Schema(
      description = "변경된 콘텐츠 상태 [DRAFT - 작성 중], [ACTIVE - 판매중], [DISCONTINUED - 판매중단]",
      example = "ACTIVE",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String status;

  @Schema(
      description = "정기 결제 판매 상태 [OPEN, PAUSED, TERMINATED]. 단건 결제 콘텐츠는 null.",
      example = "PAUSED",
      nullable = true)
  private String subscriptionSellStatus;
}
