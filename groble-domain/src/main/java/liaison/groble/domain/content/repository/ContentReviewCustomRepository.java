package liaison.groble.domain.content.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import liaison.groble.domain.content.dto.FlatContentReviewDetailDTO;

public interface ContentReviewCustomRepository {
  Optional<FlatContentReviewDetailDTO> getContentReviewDetailDTO(
      Long userId, Long contentId, Long reviewId);

  void updateContentReviewStatusToDeleteRequested(Long userId, Long reviewId);

  Page<FlatContentReviewDetailDTO> getContentReviewPageDTOs(
      Long userId, Long contentId, Pageable pageable);
}
