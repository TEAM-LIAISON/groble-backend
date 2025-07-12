package liaison.groble.application.content;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.domain.content.dto.FlatContentReviewDetailDTO;
import liaison.groble.domain.content.entity.ContentReview;
import liaison.groble.domain.content.repository.ContentReviewCustomRepository;
import liaison.groble.domain.content.repository.ContentReviewRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ContentReviewReader {
  private final ContentReviewCustomRepository contentReviewCustomRepository;
  private final ContentReviewRepository contentReviewRepository;

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

  public Optional<FlatContentReviewDetailDTO> getContentReviewDetail(Long userId, Long contentId) {
    log.info("getContentReviewDetail by contentId: {}", contentId);

    return contentReviewCustomRepository.getContentReviewDetailDTOByContentId(
        userId, contentId); // ➜ 그대로 Optional 반환
  }

  public Page<FlatContentReviewDetailDTO> getContentReviews(
      Long userId, Long contentId, Pageable pageable) {
    return contentReviewCustomRepository.getContentReviewPageDTOs(userId, contentId, pageable);
  }

  public boolean existsContentReview(Long userId, Long contentId) {
    return contentReviewRepository.existsContentReview(userId, contentId);
  }
}
