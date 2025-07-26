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
}
