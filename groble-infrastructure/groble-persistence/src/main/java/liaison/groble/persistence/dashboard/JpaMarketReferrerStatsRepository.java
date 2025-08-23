package liaison.groble.persistence.dashboard;

import org.springframework.data.jpa.repository.JpaRepository;

import liaison.groble.domain.dashboard.entity.MarketReferrerStats;

public interface JpaMarketReferrerStatsRepository
    extends JpaRepository<MarketReferrerStats, Long> {}
