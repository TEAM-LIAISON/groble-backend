package liaison.groble.persistence.market;

import org.springframework.data.jpa.repository.JpaRepository;

import liaison.groble.domain.market.entity.MarketViewLog;

public interface JpaMarketViewLogRepository extends JpaRepository<MarketViewLog, Long> {}
