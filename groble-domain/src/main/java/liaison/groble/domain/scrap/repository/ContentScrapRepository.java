package liaison.groble.domain.scrap.repository;

import liaison.groble.domain.scrap.entity.ContentScrap;

public interface ContentScrapRepository {
  ContentScrap save(ContentScrap contentScrap);

  boolean existsByUserIdAndContentId(Long userId, Long contentId);

  void deleteByUserIdAndContentId(Long userId, Long contentId);
}
