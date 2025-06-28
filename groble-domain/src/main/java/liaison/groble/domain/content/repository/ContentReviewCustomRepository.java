package liaison.groble.domain.content.repository;

import java.util.Optional;

import liaison.groble.domain.content.dto.FlatContentReviewDetailDTO;

public interface ContentReviewCustomRepository {
  Optional<FlatContentReviewDetailDTO> getContentReviewDetailDTO(
      Long userId, Long contentId, Long reviewId);
}
