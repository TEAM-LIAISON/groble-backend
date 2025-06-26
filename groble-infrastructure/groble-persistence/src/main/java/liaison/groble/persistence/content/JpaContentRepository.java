package liaison.groble.persistence.content;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import liaison.groble.domain.content.entity.Content;
import liaison.groble.domain.content.enums.ContentStatus;
import liaison.groble.domain.user.entity.User;

public interface JpaContentRepository extends JpaRepository<Content, Long> {

  Optional<Content> findByIdAndUser(Long contentId, User user);

  Optional<Content> findByIdAndStatus(Long contentId, ContentStatus status);

  Optional<Content> findByUserAndIsRepresentativeTrue(User user);

  boolean existsByUserAndStatus(User user, ContentStatus status);
}
