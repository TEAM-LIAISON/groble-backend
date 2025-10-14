package liaison.groble.persistence.dashboard;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import liaison.groble.domain.dashboard.entity.MarketViewLog;

public interface JpaMarketViewLogRepository extends JpaRepository<MarketViewLog, Long> {
  List<MarketViewLog> findByViewedAtBetween(LocalDateTime start, LocalDateTime end);

  List<MarketViewLog> findByMarketIdAndViewedAtBetween(
      Long marketId, LocalDateTime start, LocalDateTime end);

  @Query(
      "select count(distinct case when m.viewerId is not null then concat('user:', m.viewerId) "
          + "else concat('anon:', coalesce(m.visitorHash, concat(coalesce(m.viewerIp,''), '|', coalesce(m.userAgent,'')))) end) "
          + "from MarketViewLog m "
          + "where m.marketId = :marketId "
          + "and m.viewedAt >= :start and m.viewedAt < :end")
  Long countDistinctViewers(
      @Param("marketId") Long marketId,
      @Param("start") LocalDateTime start,
      @Param("end") LocalDateTime end);

  @Query(
      "select count(m) from MarketViewLog m "
          + "where m.marketId = :marketId "
          + "and m.viewedAt >= :start and m.viewedAt < :end")
  Long countViews(
      @Param("marketId") Long marketId,
      @Param("start") LocalDateTime start,
      @Param("end") LocalDateTime end);
}
