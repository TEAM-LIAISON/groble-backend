package liaison.groble.persistence.content;

import org.springframework.data.jpa.repository.JpaRepository;

import liaison.groble.domain.content.entity.Content;

public interface JpaContentRepository extends JpaRepository<Content, Long> {}
