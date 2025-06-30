package liaison.groble.api.model.sell.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "[내 스토어 - 상품 관리 - 판매 관리] 판매 관리 페이지 정보 응답")
public class SellManagePageResponse {
  private SellManageDetailResponse contentSellDetail;
  private List<ContentSellDetailResponse> contentSellList;
  private List<ContentReviewDetailResponse> contentReviewList;
}
