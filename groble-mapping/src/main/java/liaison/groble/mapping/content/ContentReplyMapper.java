package liaison.groble.mapping.content;

import org.mapstruct.Mapper;

import liaison.groble.api.model.sell.response.ReviewReplyResponse;
import liaison.groble.application.content.dto.review.ReviewReplyDTO;
import liaison.groble.mapping.config.GrobleMapperConfig;

@Mapper(config = GrobleMapperConfig.class)
public interface ContentReplyMapper {
  ReviewReplyResponse toReviewReplyResponse(ReviewReplyDTO reviewReplyDTO);
}
