package liaison.groble.mapping.sell;

import org.mapstruct.Mapper;

import liaison.groble.api.model.sell.request.ReplyContentRequest;
import liaison.groble.api.model.sell.response.ContentReviewDetailResponse;
import liaison.groble.api.model.sell.response.ContentSellDetailResponse;
import liaison.groble.api.model.sell.response.ReplyContentResponse;
import liaison.groble.application.sell.dto.ContentReviewDetailDTO;
import liaison.groble.application.sell.dto.ContentSellDetailDTO;
import liaison.groble.application.sell.dto.ReplyContentDTO;
import liaison.groble.mapping.config.GrobleMapperConfig;

@Mapper(config = GrobleMapperConfig.class)
public interface SellMapper {
  // ====== 📤 Request → DTO 변환 ======
  ReplyContentDTO toReplyContentDTO(ReplyContentRequest replyContentRequest);

  // ====== 📤 DTO → Response 변환 ======
  ContentSellDetailResponse toContentSellDetailResponse(ContentSellDetailDTO contentSellDetailDTO);

  ContentReviewDetailResponse toContentReviewDetailResponse(
      ContentReviewDetailDTO contentReviewDetailDTO);

  ReplyContentResponse toReplyContentResponse(ReplyContentDTO replyContentDTO);
}
