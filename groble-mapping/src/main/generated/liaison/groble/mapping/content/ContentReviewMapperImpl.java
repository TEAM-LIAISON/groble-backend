package liaison.groble.mapping.content;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.processing.Generated;

import org.springframework.stereotype.Component;

import liaison.groble.api.model.content.response.review.ContentDetailReviewResponse;
import liaison.groble.api.model.content.response.review.ContentReviewResponse;
import liaison.groble.api.model.sell.response.ReviewReplyResponse;
import liaison.groble.application.content.dto.review.ContentDetailReviewDTO;
import liaison.groble.application.content.dto.review.ContentReviewDTO;
import liaison.groble.application.content.dto.review.ReviewReplyDTO;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-08-28T00:29:15+0900",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.10 (Amazon.com Inc.)")
@Component
public class ContentReviewMapperImpl implements ContentReviewMapper {

  @Override
  public ContentReviewResponse toContentReviewResponse(ContentReviewDTO contentReviewDTO) {
    if (contentReviewDTO == null) {
      return null;
    }

    ContentReviewResponse.ContentReviewResponseBuilder contentReviewResponse =
        ContentReviewResponse.builder();

    if (contentReviewDTO.getAverageRating() != null) {
      contentReviewResponse.averageRating(contentReviewDTO.getAverageRating());
    }
    if (contentReviewDTO.getTotalReviewCount() != null) {
      contentReviewResponse.totalReviewCount(contentReviewDTO.getTotalReviewCount());
    }
    List<ContentDetailReviewResponse> list =
        contentDetailReviewDTOListToContentDetailReviewResponseList(contentReviewDTO.getReviews());
    if (list != null) {
      contentReviewResponse.reviews(list);
    }

    return contentReviewResponse.build();
  }

  @Override
  public ContentDetailReviewResponse toContentDetailReviewResponse(
      ContentDetailReviewDTO contentDetailReviewDTO) {
    if (contentDetailReviewDTO == null) {
      return null;
    }

    ContentDetailReviewResponse.ContentDetailReviewResponseBuilder contentDetailReviewResponse =
        ContentDetailReviewResponse.builder();

    if (contentDetailReviewDTO.getReviewId() != null) {
      contentDetailReviewResponse.reviewId(contentDetailReviewDTO.getReviewId());
    }
    if (contentDetailReviewDTO.getIsReviewManage() != null) {
      contentDetailReviewResponse.isReviewManage(contentDetailReviewDTO.getIsReviewManage());
    }
    if (contentDetailReviewDTO.getCreatedAt() != null) {
      contentDetailReviewResponse.createdAt(contentDetailReviewDTO.getCreatedAt());
    }
    if (contentDetailReviewDTO.getReviewerProfileImageUrl() != null) {
      contentDetailReviewResponse.reviewerProfileImageUrl(
          contentDetailReviewDTO.getReviewerProfileImageUrl());
    }
    if (contentDetailReviewDTO.getReviewerNickname() != null) {
      contentDetailReviewResponse.reviewerNickname(contentDetailReviewDTO.getReviewerNickname());
    }
    if (contentDetailReviewDTO.getReviewContent() != null) {
      contentDetailReviewResponse.reviewContent(contentDetailReviewDTO.getReviewContent());
    }
    if (contentDetailReviewDTO.getSelectedOptionName() != null) {
      contentDetailReviewResponse.selectedOptionName(
          contentDetailReviewDTO.getSelectedOptionName());
    }
    if (contentDetailReviewDTO.getRating() != null) {
      contentDetailReviewResponse.rating(contentDetailReviewDTO.getRating());
    }
    if (contentDetailReviewDTO.getMerchantUid() != null) {
      contentDetailReviewResponse.merchantUid(contentDetailReviewDTO.getMerchantUid());
    }
    List<ReviewReplyResponse> list =
        reviewReplyDTOListToReviewReplyResponseList(contentDetailReviewDTO.getReviewReplies());
    if (list != null) {
      contentDetailReviewResponse.reviewReplies(list);
    }

    return contentDetailReviewResponse.build();
  }

  protected List<ContentDetailReviewResponse>
      contentDetailReviewDTOListToContentDetailReviewResponseList(
          List<ContentDetailReviewDTO> list) {
    if (list == null) {
      return null;
    }

    List<ContentDetailReviewResponse> list1 =
        new ArrayList<ContentDetailReviewResponse>(list.size());
    for (ContentDetailReviewDTO contentDetailReviewDTO : list) {
      list1.add(toContentDetailReviewResponse(contentDetailReviewDTO));
    }

    return list1;
  }

  protected ReviewReplyResponse reviewReplyDTOToReviewReplyResponse(ReviewReplyDTO reviewReplyDTO) {
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

  protected List<ReviewReplyResponse> reviewReplyDTOListToReviewReplyResponseList(
      List<ReviewReplyDTO> list) {
    if (list == null) {
      return null;
    }

    List<ReviewReplyResponse> list1 = new ArrayList<ReviewReplyResponse>(list.size());
    for (ReviewReplyDTO reviewReplyDTO : list) {
      list1.add(reviewReplyDTOToReviewReplyResponse(reviewReplyDTO));
    }

    return list1;
  }
}
