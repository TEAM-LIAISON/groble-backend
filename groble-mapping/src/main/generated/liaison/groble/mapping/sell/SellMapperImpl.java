package liaison.groble.mapping.sell;

import javax.annotation.processing.Generated;

import org.springframework.stereotype.Component;

import liaison.groble.api.model.sell.request.AddReplyRequest;
import liaison.groble.api.model.sell.response.AddReplyResponse;
import liaison.groble.api.model.sell.response.ContentReviewDetailResponse;
import liaison.groble.api.model.sell.response.ContentSellDetailResponse;
import liaison.groble.application.sell.dto.AddReplyDTO;
import liaison.groble.application.sell.dto.ContentReviewDetailDTO;
import liaison.groble.application.sell.dto.ContentSellDetailDTO;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-06-29T01:10:09+0900",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.10 (Amazon.com Inc.)")
@Component
public class SellMapperImpl implements SellMapper {

  @Override
  public AddReplyDTO toAddReplyDTO(AddReplyRequest addReplyRequest) {
    if (addReplyRequest == null) {
      return null;
    }

    AddReplyDTO.AddReplyDTOBuilder addReplyDTO = AddReplyDTO.builder();

    if (addReplyRequest.getReplyContent() != null) {
      addReplyDTO.replyContent(addReplyRequest.getReplyContent());
    }

    return addReplyDTO.build();
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
  public AddReplyResponse toAddReplyResponse(AddReplyDTO addReplyDTO) {
    if (addReplyDTO == null) {
      return null;
    }

    AddReplyResponse.AddReplyResponseBuilder addReplyResponse = AddReplyResponse.builder();

    if (addReplyDTO.getReplyContent() != null) {
      addReplyResponse.replyContent(addReplyDTO.getReplyContent());
    }

    return addReplyResponse.build();
  }
}
