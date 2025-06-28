package liaison.groble.application.sell.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.content.ContentReviewReader;
import liaison.groble.application.purchase.service.PurchaseReader;
import liaison.groble.application.sell.dto.ContentReviewDetailDTO;
import liaison.groble.application.sell.dto.ContentSellDetailDTO;
import liaison.groble.domain.content.dto.FlatContentReviewDetailDTO;
import liaison.groble.domain.purchase.dto.FlatContentSellDetailDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SellContentService {

  private final ContentReviewReader contentReviewReader;
  private final PurchaseReader purchaseReader;

  @Transactional(readOnly = true)
  public ContentReviewDetailDTO getContentReviewDetail(Long userId, Long contentId, Long reviewId) {
    FlatContentReviewDetailDTO contentReviewDetailDTO =
        contentReviewReader.getContentReviewDetail(userId, contentId, reviewId);
    return buildContentReviewDetail(contentReviewDetailDTO);
  }

  @Transactional(readOnly = true)
  public ContentSellDetailDTO getContentSellDetail(Long userId, Long contentId, Long purchaseId) {
    FlatContentSellDetailDTO contentSellDetailDTO =
        purchaseReader.getContentSellDetail(userId, contentId, purchaseId);

    return buildContentSellDetail(contentSellDetailDTO);
  }

  private ContentSellDetailDTO buildContentSellDetail(
      FlatContentSellDetailDTO contentSellDetailDTO) {
    return ContentSellDetailDTO.builder()
        .purchaseId(contentSellDetailDTO.getPurchaseId())
        .contentTitle(contentSellDetailDTO.getContentTitle())
        .purchasedAt(contentSellDetailDTO.getPurchasedAt())
        .purchaserNickname(contentSellDetailDTO.getPurchaserNickname())
        .purchaserEmail(contentSellDetailDTO.getPurchaserEmail())
        .purchaserPhoneNumber(contentSellDetailDTO.getPurchaserPhoneNumber())
        .selectedOptionName(contentSellDetailDTO.getSelectedOptionName())
        .finalPrice(contentSellDetailDTO.getFinalPrice())
        .build();
  }

  private ContentReviewDetailDTO buildContentReviewDetail(
      FlatContentReviewDetailDTO flatContentReviewDetailDTO) {
    return ContentReviewDetailDTO.builder()
        .reviewId(flatContentReviewDetailDTO.getReviewId())
        .contentTitle(flatContentReviewDetailDTO.getContentTitle())
        .createdAt(flatContentReviewDetailDTO.getCreatedAt())
        .reviewerNickname(flatContentReviewDetailDTO.getReviewerNickname())
        .selectedOptionName(flatContentReviewDetailDTO.getSelectedOptionName())
        .rating(flatContentReviewDetailDTO.getRating())
        .build();
  }
}
