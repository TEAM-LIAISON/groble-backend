package liaison.groble.persistence.content;

import org.springframework.data.jpa.repository.JpaRepository;

import liaison.groble.domain.content.entity.ContentViewLog;

public interface JpaContentViewLogRepository extends JpaRepository<ContentViewLog, Long> {}
