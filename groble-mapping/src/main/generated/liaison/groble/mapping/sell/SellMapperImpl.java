package liaison.groble.mapping.sell;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.processing.Generated;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import liaison.groble.api.model.sell.request.ReplyContentRequest;
import liaison.groble.api.model.sell.response.ContentReviewDetailResponse;
import liaison.groble.api.model.sell.response.ContentSellDetailResponse;
import liaison.groble.api.model.sell.response.ReplyContentResponse;
import liaison.groble.api.model.sell.response.ReviewReplyResponse;
import liaison.groble.api.model.sell.response.SellManageDetailResponse;
import liaison.groble.api.model.sell.response.SellManagePageResponse;
import liaison.groble.application.content.dto.review.ReviewReplyDTO;
import liaison.groble.application.sell.dto.ContentReviewDetailDTO;
import liaison.groble.application.sell.dto.ContentSellDetailDTO;
import liaison.groble.application.sell.dto.ReplyContentDTO;
import liaison.groble.application.sell.dto.SellManageDetailDTO;
import liaison.groble.application.sell.dto.SellManagePageDTO;
import liaison.groble.mapping.content.ContentReplyMapper;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-09-02T17:55:20+0900",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.10 (Amazon.com Inc.)")
@Component
public class SellMapperImpl implements SellMapper {

  private final ContentReplyMapper contentReplyMapper;

  @Autowired
  public SellMapperImpl(ContentReplyMapper contentReplyMapper) {

    this.contentReplyMapper = contentReplyMapper;
  }

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
  public SellManagePageResponse toSellManagePageResponse(SellManagePageDTO sellManagePageDTO) {
    if (sellManagePageDTO == null) {
      return null;
    }

    SellManagePageResponse.SellManagePageResponseBuilder sellManagePageResponse =
        SellManagePageResponse.builder();

    if (sellManagePageDTO.getTitle() != null) {
      sellManagePageResponse.title(sellManagePageDTO.getTitle());
    }
    if (sellManagePageDTO.getSellManageDetail() != null) {
      sellManagePageResponse.contentSellDetail(
          toSellManageDetailResponse(sellManagePageDTO.getSellManageDetail()));
    }
    List<ContentSellDetailResponse> list =
        contentSellDetailDTOListToContentSellDetailResponseList(
            sellManagePageDTO.getContentSellDetailList());
    if (list != null) {
      sellManagePageResponse.contentSellList(list);
    }
    List<ContentReviewDetailResponse> list1 =
        contentReviewDetailDTOListToContentReviewDetailResponseList(
            sellManagePageDTO.getContentReviewDetailList());
    if (list1 != null) {
      sellManagePageResponse.contentReviewList(list1);
    }

    return sellManagePageResponse.build();
  }

  @Override
  public SellManageDetailResponse toSellManageDetailResponse(
      SellManageDetailDTO sellManageDetailDTO) {
    if (sellManageDetailDTO == null) {
      return null;
    }

    SellManageDetailResponse.SellManageDetailResponseBuilder sellManageDetailResponse =
        SellManageDetailResponse.builder();

    if (sellManageDetailDTO.getTotalPaymentPrice() != null) {
      sellManageDetailResponse.totalPaymentPrice(sellManageDetailDTO.getTotalPaymentPrice());
    }
    if (sellManageDetailDTO.getTotalPurchaseCustomer() != null) {
      sellManageDetailResponse.totalPurchaseCustomer(
          sellManageDetailDTO.getTotalPurchaseCustomer());
    }
    if (sellManageDetailDTO.getTotalReviewCount() != null) {
      sellManageDetailResponse.totalReviewCount(sellManageDetailDTO.getTotalReviewCount());
    }

    return sellManageDetailResponse.build();
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
    if (contentReviewDetailDTO.getReviewStatus() != null) {
      contentReviewDetailResponse.reviewStatus(contentReviewDetailDTO.getReviewStatus());
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
    if (contentReviewDetailDTO.getReviewContent() != null) {
      contentReviewDetailResponse.reviewContent(contentReviewDetailDTO.getReviewContent());
    }
    if (contentReviewDetailDTO.getSelectedOptionName() != null) {
      contentReviewDetailResponse.selectedOptionName(
          contentReviewDetailDTO.getSelectedOptionName());
    }
    if (contentReviewDetailDTO.getRating() != null) {
      contentReviewDetailResponse.rating(contentReviewDetailDTO.getRating());
    }
    List<ReviewReplyResponse> list =
        reviewReplyDTOListToReviewReplyResponseList(contentReviewDetailDTO.getReviewReplies());
    if (list != null) {
      contentReviewDetailResponse.reviewReplies(list);
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

  protected List<ContentSellDetailResponse> contentSellDetailDTOListToContentSellDetailResponseList(
      List<ContentSellDetailDTO> list) {
    if (list == null) {
      return null;
    }

    List<ContentSellDetailResponse> list1 = new ArrayList<ContentSellDetailResponse>(list.size());
    for (ContentSellDetailDTO contentSellDetailDTO : list) {
      list1.add(toContentSellDetailResponse(contentSellDetailDTO));
    }

    return list1;
  }

  protected List<ContentReviewDetailResponse>
      contentReviewDetailDTOListToContentReviewDetailResponseList(
          List<ContentReviewDetailDTO> list) {
    if (list == null) {
      return null;
    }

    List<ContentReviewDetailResponse> list1 =
        new ArrayList<ContentReviewDetailResponse>(list.size());
    for (ContentReviewDetailDTO contentReviewDetailDTO : list) {
      list1.add(toContentReviewDetailResponse(contentReviewDetailDTO));
    }

    return list1;
  }

  protected List<ReviewReplyResponse> reviewReplyDTOListToReviewReplyResponseList(
      List<ReviewReplyDTO> list) {
    if (list == null) {
      return null;
    }

    List<ReviewReplyResponse> list1 = new ArrayList<ReviewReplyResponse>(list.size());
    for (ReviewReplyDTO reviewReplyDTO : list) {
      list1.add(contentReplyMapper.toReviewReplyResponse(reviewReplyDTO));
    }

    return list1;
  }
}
