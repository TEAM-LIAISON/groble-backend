package liaison.groble.persistence.content;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import liaison.groble.domain.content.entity.Content;
import liaison.groble.domain.content.enums.ContentStatus;
import liaison.groble.domain.user.entity.User;

public interface JpaContentRepository extends JpaRepository<Content, Long> {

  Optional<Content> findByIdAndUser(Long contentId, User user);

  @Query("SELECT c FROM Content c JOIN FETCH c.user u WHERE c.id = :contentId")
  Optional<Content> findByIdWithSeller(@Param("contentId") Long contentId);

  Optional<Content> findByIdAndStatus(Long contentId, ContentStatus status);

  Optional<Content> findByUserAndIsRepresentativeTrue(User user);

  @Query("SELECT c.id FROM Content c WHERE c.user.id = :userId")
  List<Long> findIdsByUserId(@Param("userId") Long userId);

  boolean existsByUserAndStatus(User user, ContentStatus status);
}
