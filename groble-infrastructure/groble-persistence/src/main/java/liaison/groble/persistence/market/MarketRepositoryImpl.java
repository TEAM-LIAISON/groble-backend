package liaison.groble.persistence.market;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import liaison.groble.domain.market.entity.Market;
import liaison.groble.domain.market.repository.MarketRepository;

import lombok.AllArgsConstructor;

@Repository
@AllArgsConstructor
public class MarketRepositoryImpl implements MarketRepository {
  private final JpaMarketRepository jpaMarketRepository;

  @Override
  public Optional<Market> findByUserId(Long userId) {
    return jpaMarketRepository.findByUserId(userId);
  }

  @Override
  public Optional<Market> findByMarketLinkUrl(String marketLinkUrl) {
    return jpaMarketRepository.findByMarketLinkUrl(marketLinkUrl);
  }

  @Override
  public boolean existsByMarketLinkUrl(String marketLinkUrl) {
    return jpaMarketRepository.existsByMarketLinkUrl(marketLinkUrl);
  }

  @Override
  public boolean existsByUserId(Long userId) {
    return jpaMarketRepository.existsByUserId(userId);
  }

  @Override
  public Market save(Market market) {
    return jpaMarketRepository.save(market);
  }
}
