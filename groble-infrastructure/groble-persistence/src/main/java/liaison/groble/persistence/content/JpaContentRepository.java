package liaison.groble.persistence.content;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import liaison.groble.domain.content.entity.Content;
import liaison.groble.domain.content.enums.ContentStatus;

public interface JpaContentRepository extends JpaRepository<Content, Long> {

  Optional<Content> findByIdAndStatus(Long contentId, ContentStatus status);
}
