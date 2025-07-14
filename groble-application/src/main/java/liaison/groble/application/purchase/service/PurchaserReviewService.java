package liaison.groble.application.purchase.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.content.ContentReader;
import liaison.groble.application.content.ContentReviewReader;
import liaison.groble.application.content.ContentReviewWriter;
import liaison.groble.application.purchase.dto.PurchaserContentReviewDTO;
import liaison.groble.application.user.service.UserReader;
import liaison.groble.domain.content.entity.Content;
import liaison.groble.domain.content.entity.ContentReview;
import liaison.groble.domain.content.enums.ReviewStatus;
import liaison.groble.domain.user.entity.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PurchaserReviewService {

  // Reader
  private final UserReader userReader;
  private final ContentReader contentReader;
  private final ContentReviewReader contentReviewReader;
  private final PurchaseReader purchaseReader;

  // Writer
  private final ContentReviewWriter contentReviewWriter;

  @Transactional
  public PurchaserContentReviewDTO addReview(
      Long userId, Long contentId, PurchaserContentReviewDTO purchaserContentReviewDTO) {

    User user = userReader.getUserById(userId);
    Content content = contentReader.getContentById(contentId);

    if (!purchaseReader.isContentPurchasedByUser(userId, contentId)) {
      throw new IllegalArgumentException("사용자가 해당 콘텐츠를 구매하지 않았습니다.");
    }

    if (contentReviewReader.existsContentReview(userId, contentId)) {
      throw new IllegalArgumentException("이미 해당 콘텐츠에 대한 리뷰가 존재합니다.");
    }

    ContentReview contentReview =
        ContentReview.builder()
            .user(user)
            .content(content)
            .rating(purchaserContentReviewDTO.getRating())
            .reviewContent(purchaserContentReviewDTO.getReviewContent())
            .reviewStatus(ReviewStatus.ACTIVE)
            .build();

    ContentReview savedContentReview = contentReviewWriter.save(contentReview);

    return PurchaserContentReviewDTO.builder()
        .rating(savedContentReview.getRating())
        .reviewContent(savedContentReview.getReviewContent())
        .build();
  }

  @Transactional
  public PurchaserContentReviewDTO updateReview(
      Long userId,
      Long contentId,
      Long reviewId,
      PurchaserContentReviewDTO purchaserContentReviewDTO) {
    ContentReview contentReview = contentReviewReader.getContentReview(userId, contentId, reviewId);
    contentReview.updateReview(
        purchaserContentReviewDTO.getRating(), purchaserContentReviewDTO.getReviewContent());

    return PurchaserContentReviewDTO.builder()
        .rating(purchaserContentReviewDTO.getRating())
        .reviewContent(purchaserContentReviewDTO.getReviewContent())
        .build();
  }

  @Transactional
  public void deleteReview(Long userId, Long contentId, Long reviewId) {
    ContentReview contentReview = contentReviewReader.getContentReview(userId, contentId, reviewId);
    contentReviewWriter.deleteContentReview(userId, contentReview.getId());
  }
}
