package liaison.groble.domain.content.repository;

import java.util.List;
import java.util.Optional;

import liaison.groble.domain.content.dto.FlatContentPreviewDTO;
import liaison.groble.domain.content.entity.Content;
import liaison.groble.domain.content.enums.ContentStatus;

public interface ContentRepository {
  Optional<Content> findById(Long contentId);

  Optional<Content> findByIdAndStatus(Long contentId, ContentStatus status);

  Content save(Content content);

  void delete(Content content);

  Optional<FlatContentPreviewDTO> findFlatContentById(Long contentId);

  List<FlatContentPreviewDTO> findFlatContentsByUserId(Long userId);
}
