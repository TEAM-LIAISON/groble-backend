package liaison.groble.persistence.scrap;

import org.springframework.data.jpa.repository.JpaRepository;

import liaison.groble.domain.scrap.entity.ContentScrap;

public interface JpaContentScrapRepository extends JpaRepository<ContentScrap, Long> {

  boolean existsByUserIdAndContentId(Long userId, Long contentId);

  void deleteByUserIdAndContentId(Long userId, Long contentId);
}
