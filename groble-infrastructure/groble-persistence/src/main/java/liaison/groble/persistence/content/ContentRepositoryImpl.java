package liaison.groble.persistence.content;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import liaison.groble.domain.content.dto.FlatContentPreviewDTO;
import liaison.groble.domain.content.entity.Content;
import liaison.groble.domain.content.enums.ContentStatus;
import liaison.groble.domain.content.repository.ContentCustomRepository;
import liaison.groble.domain.content.repository.ContentRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ContentRepositoryImpl implements ContentRepository {
  private final JpaContentRepository jpaContentRepository;
  private final ContentCustomRepository contentCustomRepository;

  @Override
  public Optional<Content> findById(Long contentId) {
    return jpaContentRepository.findById(contentId);
  }

  @Override
  public Optional<Content> findByIdAndStatus(Long contentId, ContentStatus status) {
    return jpaContentRepository.findByIdAndStatus(contentId, status);
  }

  @Override
  public Content save(Content content) {
    return jpaContentRepository.save(content);
  }

  @Override
  public void delete(Content content) {
    jpaContentRepository.delete(content);
  }

  public Optional<FlatContentPreviewDTO> findFlatContentById(Long contentId) {
    return contentCustomRepository.findFlatContentById(contentId);
  }

  public List<FlatContentPreviewDTO> findFlatContentsByUserId(Long userId) {
    return contentCustomRepository.findFlatContentsByUserId(userId);
  }
}
