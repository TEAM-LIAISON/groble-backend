package liaison.groble.persistence.content;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import liaison.groble.domain.content.dto.FlatContentPreviewDTO;
import liaison.groble.domain.content.entity.Content;
import liaison.groble.domain.content.enums.ContentStatus;
import liaison.groble.domain.content.repository.ContentCustomRepository;
import liaison.groble.domain.content.repository.ContentRepository;
import liaison.groble.domain.user.entity.User;

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
  public Optional<Content> findByIdAndUser(Long contentId, User user) {
    return jpaContentRepository.findByIdAndUser(contentId, user);
  }

  @Override
  public Optional<Content> findByIdWithSeller(Long contentId) {
    return jpaContentRepository.findByIdWithSeller(contentId);
  }

  @Override
  public boolean existsByUserAndStatus(User user, ContentStatus status) {
    return jpaContentRepository.existsByUserAndStatus(user, status);
  }

  @Override
  public Optional<Content> findByIdAndStatus(Long contentId, ContentStatus status) {
    return jpaContentRepository.findByIdAndStatus(contentId, status);
  }

  @Override
  public Optional<Content> findByUserAndIsRepresentativeTrue(User user) {
    return jpaContentRepository.findByUserAndIsRepresentativeTrue(user);
  }

  @Override
  public Content save(Content content) {
    return jpaContentRepository.save(content);
  }

  @Override
  public void delete(Content content) {
    jpaContentRepository.delete(content);
  }

  @Override
  public List<Long> findIdsByUserId(Long userId) {
    return jpaContentRepository.findIdsByUserId(userId);
  }

  public boolean existsSellingContentByUser(Long userId) {
    return contentCustomRepository.existsSellingContentByUser(userId);
  }

  public Optional<FlatContentPreviewDTO> findFlatContentById(Long contentId) {
    return contentCustomRepository.findFlatContentById(contentId);
  }

  public List<FlatContentPreviewDTO> findFlatContentsByUserId(Long userId) {
    return contentCustomRepository.findFlatContentsByUserId(userId);
  }
}
