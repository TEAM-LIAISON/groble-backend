package liaison.groble.mapping.sell;

import org.mapstruct.Mapper;

import liaison.groble.api.model.sell.request.AddReplyRequest;
import liaison.groble.api.model.sell.response.AddReplyResponse;
import liaison.groble.api.model.sell.response.ContentReviewDetailResponse;
import liaison.groble.api.model.sell.response.ContentSellDetailResponse;
import liaison.groble.application.sell.dto.AddReplyDTO;
import liaison.groble.application.sell.dto.ContentReviewDetailDTO;
import liaison.groble.application.sell.dto.ContentSellDetailDTO;
import liaison.groble.mapping.config.GrobleMapperConfig;

@Mapper(config = GrobleMapperConfig.class)
public interface SellMapper {
  // ====== 📤 Request → DTO 변환 ======
  AddReplyDTO toAddReplyDTO(AddReplyRequest addReplyRequest);

  // ====== 📤 DTO → Response 변환 ======
  ContentSellDetailResponse toContentSellDetailResponse(ContentSellDetailDTO contentSellDetailDTO);

  ContentReviewDetailResponse toContentReviewDetailResponse(
      ContentReviewDetailDTO contentReviewDetailDTO);

  AddReplyResponse toAddReplyResponse(AddReplyDTO addReplyDTO);
}
