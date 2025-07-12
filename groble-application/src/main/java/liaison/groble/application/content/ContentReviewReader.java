package liaison.groble.application.content;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.domain.content.dto.FlatContentReviewDetailDTO;
import liaison.groble.domain.content.entity.ContentReview;
import liaison.groble.domain.content.repository.ContentReviewCustomRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContentReviewReader {
  private final ContentReviewCustomRepository contentReviewCustomRepository;

  public ContentReview getContentReview(Long userId, Long contentId, Long reviewId) {
    return contentReviewCustomRepository
        .getContentReview(userId, contentId, reviewId)
        .orElseThrow(
            () ->
                new IllegalArgumentException(
                    "리뷰를 찾을 수 없습니다. User ID: "
                        + userId
                        + ", Content ID: "
                        + contentId
                        + ", Review ID: "
                        + reviewId));
  }

  public FlatContentReviewDetailDTO getContentReviewDetail(
      Long userId, Long contentId, Long reviewId) {
    return contentReviewCustomRepository
        .getContentReviewDetailDTO(userId, contentId, reviewId)
        .orElseThrow(
            () ->
                new IllegalArgumentException(
                    "리뷰 상세 정보를 찾을 수 없습니다. User ID: "
                        + userId
                        + ", Content ID: "
                        + contentId
                        + ", Review ID: "
                        + reviewId));
  }

  public FlatContentReviewDetailDTO getContentReviewDetail(Long userId, String merchantUid) {
    return contentReviewCustomRepository
        .getContentReviewDetailDTOByMerchantUid(userId, merchantUid)
        .orElseThrow(
            () ->
                new IllegalArgumentException(
                    "리뷰 상세 정보를 찾을 수 없습니다. User ID: " + userId + ", Merchant UID: " + merchantUid));
  }

  public Page<FlatContentReviewDetailDTO> getContentReviews(
      Long userId, Long contentId, Pageable pageable) {
    return contentReviewCustomRepository.getContentReviewPageDTOs(userId, contentId, pageable);
  }
}
