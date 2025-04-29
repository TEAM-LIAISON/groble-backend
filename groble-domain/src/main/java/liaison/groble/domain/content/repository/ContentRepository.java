package liaison.groble.domain.content.repository;

import java.util.List;
import java.util.Optional;

import liaison.groble.domain.content.dto.FlatPreviewContentDTO;
import liaison.groble.domain.content.entity.Content;
import liaison.groble.domain.content.enums.ContentStatus;

public interface ContentRepository {
  Optional<Content> findById(Long contentId);

  Optional<Content> findByIdAndStatus(Long contentId, ContentStatus status);

  Content save(Content content);

  Optional<FlatPreviewContentDTO> findFlatContentById(Long contentId);

  List<FlatPreviewContentDTO> findFlatContentsByUserId(Long userId);
}
