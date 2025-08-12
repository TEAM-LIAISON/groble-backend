package liaison.groble.persistence.market;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import liaison.groble.domain.market.entity.Market;

public interface JpaMarketRepository extends JpaRepository<Market, Long> {
  Optional<Market> findByUserId(Long userId);

  @Query("SELECT m FROM Market m JOIN FETCH m.user WHERE m.marketLinkUrl = :marketLinkUrl")
  Optional<Market> findByMarketLinkUrl(@Param("marketLinkUrl") String marketLinkUrl);

  boolean existsByMarketLinkUrl(String marketLinkUrl);

  boolean existsByUserId(Long userId);
}
