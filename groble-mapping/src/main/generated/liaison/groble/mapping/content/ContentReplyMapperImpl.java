package liaison.groble.mapping.content;

import javax.annotation.processing.Generated;

import org.springframework.stereotype.Component;

import liaison.groble.api.model.sell.response.ReviewReplyResponse;
import liaison.groble.application.content.dto.review.ReviewReplyDTO;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-07-25T22:40:31+0900",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.10 (Amazon.com Inc.)")
@Component
public class ContentReplyMapperImpl implements ContentReplyMapper {

  @Override
  public ReviewReplyResponse toReviewReplyResponse(ReviewReplyDTO reviewReplyDTO) {
    if (reviewReplyDTO == null) {
      return null;
    }

    ReviewReplyResponse.ReviewReplyResponseBuilder reviewReplyResponse =
        ReviewReplyResponse.builder();

    if (reviewReplyDTO.getReplyId() != null) {
      reviewReplyResponse.replyId(reviewReplyDTO.getReplyId());
    }
    if (reviewReplyDTO.getCreatedAt() != null) {
      reviewReplyResponse.createdAt(reviewReplyDTO.getCreatedAt());
    }
    if (reviewReplyDTO.getReplierNickname() != null) {
      reviewReplyResponse.replierNickname(reviewReplyDTO.getReplierNickname());
    }
    if (reviewReplyDTO.getReplyContent() != null) {
      reviewReplyResponse.replyContent(reviewReplyDTO.getReplyContent());
    }

    return reviewReplyResponse.build();
  }
}
