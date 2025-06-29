package liaison.groble.mapping.sell;

import javax.annotation.processing.Generated;

import org.springframework.stereotype.Component;

import liaison.groble.api.model.sell.request.ReplyContentRequest;
import liaison.groble.api.model.sell.response.ContentReviewDetailResponse;
import liaison.groble.api.model.sell.response.ContentSellDetailResponse;
import liaison.groble.api.model.sell.response.ReplyContentResponse;
import liaison.groble.application.sell.dto.ContentReviewDetailDTO;
import liaison.groble.application.sell.dto.ContentSellDetailDTO;
import liaison.groble.application.sell.dto.ReplyContentDTO;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-06-29T17:45:16+0900",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.10 (Amazon.com Inc.)")
@Component
public class SellMapperImpl implements SellMapper {

  @Override
  public ReplyContentDTO toReplyContentDTO(ReplyContentRequest replyContentRequest) {
    if (replyContentRequest == null) {
      return null;
    }

    ReplyContentDTO.ReplyContentDTOBuilder replyContentDTO = ReplyContentDTO.builder();

    if (replyContentRequest.getReplyContent() != null) {
      replyContentDTO.replyContent(replyContentRequest.getReplyContent());
    }

    return replyContentDTO.build();
  }

  @Override
  public ContentSellDetailResponse toContentSellDetailResponse(
      ContentSellDetailDTO contentSellDetailDTO) {
    if (contentSellDetailDTO == null) {
      return null;
    }

    ContentSellDetailResponse.ContentSellDetailResponseBuilder contentSellDetailResponse =
        ContentSellDetailResponse.builder();

    if (contentSellDetailDTO.getPurchaseId() != null) {
      contentSellDetailResponse.purchaseId(contentSellDetailDTO.getPurchaseId());
    }
    if (contentSellDetailDTO.getContentTitle() != null) {
      contentSellDetailResponse.contentTitle(contentSellDetailDTO.getContentTitle());
    }
    if (contentSellDetailDTO.getPurchasedAt() != null) {
      contentSellDetailResponse.purchasedAt(contentSellDetailDTO.getPurchasedAt());
    }
    if (contentSellDetailDTO.getPurchaserNickname() != null) {
      contentSellDetailResponse.purchaserNickname(contentSellDetailDTO.getPurchaserNickname());
    }
    if (contentSellDetailDTO.getPurchaserEmail() != null) {
      contentSellDetailResponse.purchaserEmail(contentSellDetailDTO.getPurchaserEmail());
    }
    if (contentSellDetailDTO.getPurchaserPhoneNumber() != null) {
      contentSellDetailResponse.purchaserPhoneNumber(
          contentSellDetailDTO.getPurchaserPhoneNumber());
    }
    if (contentSellDetailDTO.getSelectedOptionName() != null) {
      contentSellDetailResponse.selectedOptionName(contentSellDetailDTO.getSelectedOptionName());
    }
    if (contentSellDetailDTO.getFinalPrice() != null) {
      contentSellDetailResponse.finalPrice(contentSellDetailDTO.getFinalPrice());
    }

    return contentSellDetailResponse.build();
  }

  @Override
  public ContentReviewDetailResponse toContentReviewDetailResponse(
      ContentReviewDetailDTO contentReviewDetailDTO) {
    if (contentReviewDetailDTO == null) {
      return null;
    }

    ContentReviewDetailResponse.ContentReviewDetailResponseBuilder contentReviewDetailResponse =
        ContentReviewDetailResponse.builder();

    if (contentReviewDetailDTO.getReviewId() != null) {
      contentReviewDetailResponse.reviewId(contentReviewDetailDTO.getReviewId());
    }
    if (contentReviewDetailDTO.getContentTitle() != null) {
      contentReviewDetailResponse.contentTitle(contentReviewDetailDTO.getContentTitle());
    }
    if (contentReviewDetailDTO.getCreatedAt() != null) {
      contentReviewDetailResponse.createdAt(contentReviewDetailDTO.getCreatedAt());
    }
    if (contentReviewDetailDTO.getReviewerNickname() != null) {
      contentReviewDetailResponse.reviewerNickname(contentReviewDetailDTO.getReviewerNickname());
    }
    if (contentReviewDetailDTO.getSelectedOptionName() != null) {
      contentReviewDetailResponse.selectedOptionName(
          contentReviewDetailDTO.getSelectedOptionName());
    }
    if (contentReviewDetailDTO.getRating() != null) {
      contentReviewDetailResponse.rating(contentReviewDetailDTO.getRating());
    }

    return contentReviewDetailResponse.build();
  }

  @Override
  public ReplyContentResponse toReplyContentResponse(ReplyContentDTO replyContentDTO) {
    if (replyContentDTO == null) {
      return null;
    }

    ReplyContentResponse.ReplyContentResponseBuilder replyContentResponse =
        ReplyContentResponse.builder();

    if (replyContentDTO.getReplyContent() != null) {
      replyContentResponse.replyContent(replyContentDTO.getReplyContent());
    }

    return replyContentResponse.build();
  }
}
