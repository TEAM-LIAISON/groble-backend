package liaison.groble.persistence.content;

import org.springframework.data.jpa.repository.JpaRepository;

import liaison.groble.domain.content.entity.ContentViewStats;

public interface JpaContentViewStatsRepository extends JpaRepository<ContentViewStats, Long> {}
