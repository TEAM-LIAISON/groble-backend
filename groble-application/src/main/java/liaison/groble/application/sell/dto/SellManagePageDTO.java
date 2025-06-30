package liaison.groble.application.sell.dto;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SellManagePageDTO {
  private SellManageDetailDTO sellManageDetail; // SellManageDetailDTO 정보
  private List<ContentSellDetailDTO> contentSellDetailList; // ContentSellDetailDTO 목록
  private List<ContentReviewDetailDTO> contentReviewDetailList; // ContentReviewDetailDTO 목록
}
