package liaison.groble.persistence.dashboard;

import org.springframework.data.jpa.repository.JpaRepository;

import liaison.groble.domain.dashboard.entity.ContentReferrerStats;

public interface JpaContentReferrerStatsRepository
    extends JpaRepository<ContentReferrerStats, Long> {}
