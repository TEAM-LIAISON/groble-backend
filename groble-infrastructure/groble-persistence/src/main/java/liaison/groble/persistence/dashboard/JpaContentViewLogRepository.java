package liaison.groble.persistence.dashboard;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import liaison.groble.domain.dashboard.entity.ContentViewLog;

public interface JpaContentViewLogRepository extends JpaRepository<ContentViewLog, Long> {
  List<ContentViewLog> findByViewedAtBetween(LocalDateTime start, LocalDateTime end);

  List<ContentViewLog> findByContentIdInAndViewedAtBetween(
      List<Long> contentIds, LocalDateTime start, LocalDateTime end);

  @Query(
      "select count(distinct case when c.viewerId is not null then concat('user:', c.viewerId) "
          + "else concat('anon:', coalesce(c.visitorHash, concat(coalesce(c.viewerIp,''), '|', coalesce(c.userAgent,'')))) end) "
          + "from ContentViewLog c "
          + "where c.contentId in :contentIds "
          + "and c.viewedAt >= :start and c.viewedAt < :end")
  Long countDistinctViewers(
      @Param("contentIds") List<Long> contentIds,
      @Param("start") LocalDateTime start,
      @Param("end") LocalDateTime end);

  @Query(
      "select count(c) from ContentViewLog c "
          + "where c.contentId in :contentIds "
          + "and c.viewedAt >= :start and c.viewedAt < :end")
  Long countViews(
      @Param("contentIds") List<Long> contentIds,
      @Param("start") LocalDateTime start,
      @Param("end") LocalDateTime end);
}
