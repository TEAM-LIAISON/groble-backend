package liaison.groble.persistence.scrap;

import org.springframework.stereotype.Repository;

import liaison.groble.domain.scrap.entity.ContentScrap;
import liaison.groble.domain.scrap.repository.ContentScrapRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ContentScrapRepositoryImpl implements ContentScrapRepository {
  private final JpaContentScrapRepository jpaContentScrapRepository;

  @Override
  public boolean existsByUserIdAndContentId(Long userId, Long contentId) {
    return jpaContentScrapRepository.existsByUserIdAndContentId(userId, contentId);
  }

  @Override
  public void deleteByUserIdAndContentId(Long userId, Long contentId) {
    jpaContentScrapRepository.deleteByUserIdAndContentId(userId, contentId);
  }

  @Override
  public ContentScrap save(ContentScrap contentScrap) {
    return jpaContentScrapRepository.save(contentScrap);
  }
}
