package liaison.groble.domain.content.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import liaison.groble.domain.content.dto.FlatContentReviewDetailDTO;
import liaison.groble.domain.content.dto.FlatContentReviewReplyDTO;
import liaison.groble.domain.content.entity.ContentReview;

public interface ContentReviewCustomRepository {
  Optional<ContentReview> getContentReview(Long userId, Long reviewId);

  Optional<ContentReview> getContentReviewForGuest(Long guestUserId, Long reviewId);

  Optional<FlatContentReviewDetailDTO> getContentReviewDetailDTO(
      Long userId, Long contentId, Long reviewId);

  Optional<FlatContentReviewDetailDTO> getContentReviewDetailDTOByContentId(
      Long userId, Long contentId);

  List<FlatContentReviewReplyDTO> findReviewsWithRepliesByContentId(Long contentId);

  void updateContentReviewStatusToDeleteRequested(Long userId, Long reviewId);

  void deleteContentReview(Long userId, Long reviewId);

  void deleteGuestContentReview(Long guestUserId, Long reviewId);

  Page<FlatContentReviewDetailDTO> getContentReviewPageDTOs(
      Long userId, Long contentId, Pageable pageable);
}
