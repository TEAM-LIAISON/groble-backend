package liaison.groble.application.purchase.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.content.ContentReviewReader;
import liaison.groble.application.content.ContentReviewWriter;
import liaison.groble.application.purchase.dto.PurchaserContentReviewDTO;
import liaison.groble.domain.content.entity.ContentReview;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PurchaserReviewService {

  private final ContentReviewReader contentReviewReader;
  private final ContentReviewWriter contentReviewWriter;

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
