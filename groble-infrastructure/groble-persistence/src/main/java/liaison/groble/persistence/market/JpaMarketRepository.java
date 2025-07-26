package liaison.groble.persistence.market;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import liaison.groble.domain.market.entity.Market;

public interface JpaMarketRepository extends JpaRepository<Market, Long> {
  Optional<Market> findByUserId(Long userId);
}
