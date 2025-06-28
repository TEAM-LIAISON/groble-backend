package liaison.groble.mapping.sell;

import org.mapstruct.Mapper;

import liaison.groble.api.model.sell.response.ContentReviewDetailResponse;
import liaison.groble.api.model.sell.response.ContentSellDetailResponse;
import liaison.groble.application.sell.dto.ContentReviewDetailDTO;
import liaison.groble.application.sell.dto.ContentSellDetailDTO;
import liaison.groble.mapping.config.GrobleMapperConfig;

@Mapper(config = GrobleMapperConfig.class)
public interface SellMapper {
  // ====== 📤 DTO → Response 변환 ======
  ContentSellDetailResponse toContentSellDetailResponse(ContentSellDetailDTO contentSellDetailDTO);

  ContentReviewDetailResponse toContentReviewDetailResponse(
      ContentReviewDetailDTO contentReviewDetailDTO);
}
