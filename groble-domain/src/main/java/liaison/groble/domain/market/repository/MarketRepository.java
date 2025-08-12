package liaison.groble.domain.market.repository;

import java.util.Optional;

import liaison.groble.domain.market.entity.Market;

public interface MarketRepository {
  Optional<Market> findByUserId(Long userId);

  Optional<Market> findByMarketLinkUrl(String marketLinkUrl);

  boolean existsByMarketLinkUrl(String marketLinkUrl);

  boolean existsByUserId(Long userId);

  Market save(Market market);
}
