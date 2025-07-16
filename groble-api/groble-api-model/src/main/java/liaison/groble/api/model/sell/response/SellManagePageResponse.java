package liaison.groble.api.model.sell.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "[내 스토어 - 상품 관리 - 판매 관리] 판매 관리 페이지 정보 응답")
public class SellManagePageResponse {
  @Schema(
      description = "특정 상품 제목",
      example = "상품 제목입니다.",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String title;

  @Schema(description = "판매 관리 상세 정보", implementation = SellManageDetailResponse.class)
  private SellManageDetailResponse contentSellDetail;

  @Schema(
      description = "판매 관리 - 판매 목록",
      implementation = ContentSellDetailResponse.class,
      type = "array")
  private List<ContentSellDetailResponse> contentSellList;

  @Schema(
      description = "판매 관리 - 판매 문의 목록",
      implementation = ContentReviewDetailResponse.class,
      type = "array")
  private List<ContentReviewDetailResponse> contentReviewList;
}
