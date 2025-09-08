package liaison.groble.persistence.dashboard;

import org.springframework.data.jpa.repository.JpaRepository;

import liaison.groble.domain.dashboard.entity.MarketReferrerEvent;

public interface JpaMarketReferrerEventRepository
    extends JpaRepository<MarketReferrerEvent, Long> {}
