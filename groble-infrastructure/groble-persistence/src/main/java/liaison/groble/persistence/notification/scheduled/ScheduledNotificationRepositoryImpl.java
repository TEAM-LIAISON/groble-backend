package liaison.groble.persistence.notification.scheduled;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import liaison.groble.domain.notification.scheduled.dto.ScheduledNotificationChannelAggregate;
import liaison.groble.domain.notification.scheduled.dto.ScheduledNotificationStatisticsAggregate;
import liaison.groble.domain.notification.scheduled.entity.ScheduledNotification;
import liaison.groble.domain.notification.scheduled.enums.ScheduledNotificationChannel;
import liaison.groble.domain.notification.scheduled.enums.ScheduledNotificationStatus;
import liaison.groble.domain.notification.scheduled.repository.ScheduledNotificationRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ScheduledNotificationRepositoryImpl implements ScheduledNotificationRepository {

  private final JpaScheduledNotificationRepository jpaScheduledNotificationRepository;

  @PersistenceContext private EntityManager entityManager;

  @Override
  public ScheduledNotification save(ScheduledNotification scheduledNotification) {
    return jpaScheduledNotificationRepository.save(scheduledNotification);
  }

  @Override
  public Optional<ScheduledNotification> findById(Long id) {
    return jpaScheduledNotificationRepository.findById(id);
  }

  @Override
  public Page<ScheduledNotification> findAll(Pageable pageable) {
    return jpaScheduledNotificationRepository.findAll(pageable);
  }

  @Override
  public long countByStatus(ScheduledNotificationStatus status) {
    return jpaScheduledNotificationRepository.countByStatus(status);
  }

  @Override
  public long countAll() {
    return jpaScheduledNotificationRepository.count();
  }

  @Override
  public ScheduledNotificationStatisticsAggregate aggregateStatistics(
      LocalDateTime startDateTime,
      LocalDateTime endDateTime,
      ScheduledNotificationChannel channel) {
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<Tuple> cq = cb.createTupleQuery();
    Root<ScheduledNotification> root = cq.from(ScheduledNotification.class);

    Expression<Long> totalScheduled = cb.count(root);
    Expression<Long> totalSent = buildStatusCount(cb, root, ScheduledNotificationStatus.SENT);
    Expression<Long> totalCancelled =
        buildStatusCount(cb, root, ScheduledNotificationStatus.CANCELLED);

    cq.multiselect(
        totalScheduled.alias("totalScheduled"),
        totalSent.alias("totalSent"),
        totalCancelled.alias("totalCancelled"));

    List<Predicate> predicates = buildPredicates(cb, root, startDateTime, endDateTime, channel);
    if (!predicates.isEmpty()) {
      cq.where(predicates.toArray(new Predicate[0]));
    }

    List<Tuple> tuples = entityManager.createQuery(cq).getResultList();
    if (tuples.isEmpty()) {
      return new ScheduledNotificationStatisticsAggregate(0L, 0L, 0L);
    }
    Tuple tuple = tuples.get(0);
    return new ScheduledNotificationStatisticsAggregate(
        getLong(tuple, "totalScheduled"),
        getLong(tuple, "totalSent"),
        getLong(tuple, "totalCancelled"));
  }

  @Override
  public List<ScheduledNotificationChannelAggregate> aggregateStatisticsByChannel(
      LocalDateTime startDateTime,
      LocalDateTime endDateTime,
      ScheduledNotificationChannel channel) {
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<Tuple> cq = cb.createTupleQuery();
    Root<ScheduledNotification> root = cq.from(ScheduledNotification.class);

    Expression<ScheduledNotificationChannel> channelExpression = root.get("channel");
    Expression<Long> totalScheduled = cb.count(root);
    Expression<Long> totalSent = buildStatusCount(cb, root, ScheduledNotificationStatus.SENT);
    Expression<Long> totalCancelled =
        buildStatusCount(cb, root, ScheduledNotificationStatus.CANCELLED);

    cq.multiselect(
        channelExpression.alias("channel"),
        totalScheduled.alias("totalScheduled"),
        totalSent.alias("totalSent"),
        totalCancelled.alias("totalCancelled"));

    List<Predicate> predicates = buildPredicates(cb, root, startDateTime, endDateTime, channel);
    if (!predicates.isEmpty()) {
      cq.where(predicates.toArray(new Predicate[0]));
    }
    cq.groupBy(channelExpression);

    List<Tuple> tuples = entityManager.createQuery(cq).getResultList();
    List<ScheduledNotificationChannelAggregate> results = new ArrayList<>();
    for (Tuple tuple : tuples) {
      ScheduledNotificationChannel channelValue =
          tuple.get("channel", ScheduledNotificationChannel.class);
      results.add(
          new ScheduledNotificationChannelAggregate(
              channelValue,
              getLong(tuple, "totalScheduled"),
              getLong(tuple, "totalSent"),
              getLong(tuple, "totalCancelled")));
    }
    return results;
  }

  private List<Predicate> buildPredicates(
      CriteriaBuilder cb,
      Root<ScheduledNotification> root,
      LocalDateTime startDateTime,
      LocalDateTime endDateTime,
      ScheduledNotificationChannel channel) {
    List<Predicate> predicates = new ArrayList<>();
    if (startDateTime != null) {
      predicates.add(cb.greaterThanOrEqualTo(root.get("scheduledAt"), startDateTime));
    }
    if (endDateTime != null) {
      predicates.add(cb.lessThanOrEqualTo(root.get("scheduledAt"), endDateTime));
    }
    if (channel != null) {
      predicates.add(cb.equal(root.get("channel"), channel));
    }
    return predicates;
  }

  private Expression<Long> buildStatusCount(
      CriteriaBuilder cb, Root<ScheduledNotification> root, ScheduledNotificationStatus status) {
    return cb.sum(
        cb.<Long>selectCase().when(cb.equal(root.get("status"), status), 1L).otherwise(0L));
  }

  private long getLong(Tuple tuple, String alias) {
    Number number = tuple.get(alias, Number.class);
    return number == null ? 0L : number.longValue();
  }
}
