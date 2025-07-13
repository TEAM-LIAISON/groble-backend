package liaison.groble.mapping.content;

import org.mapstruct.Mapper;

import liaison.groble.api.model.content.response.review.ContentDetailReviewResponse;
import liaison.groble.api.model.content.response.review.ContentReviewResponse;
import liaison.groble.api.model.sell.response.ReviewReplyResponse;
import liaison.groble.application.content.dto.review.ContentDetailReviewDTO;
import liaison.groble.application.content.dto.review.ContentReviewDTO;
import liaison.groble.application.content.dto.review.ReviewReplyDTO;
import liaison.groble.mapping.config.GrobleMapperConfig;

@Mapper(config = GrobleMapperConfig.class)
public interface ContentReviewMapper {
  ContentReviewResponse toContentReviewResponse(ContentReviewDTO contentReviewDTO);

  ContentDetailReviewResponse toContentDetailReviewResponse(
      ContentDetailReviewDTO contentDetailReviewDTO);

  ReviewReplyResponse toReviewReplyResponse(ReviewReplyDTO reviewReplyDTO);
}
