package liaison.groble.persistence.notification.scheduled;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import liaison.groble.domain.notification.scheduled.entity.ScheduledNotificationSegment;
import liaison.groble.domain.notification.scheduled.repository.ScheduledNotificationSegmentRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ScheduledNotificationSegmentRepositoryImpl
    implements ScheduledNotificationSegmentRepository {

  private final JpaScheduledNotificationSegmentRepository jpaScheduledNotificationSegmentRepository;

  @Override
  public ScheduledNotificationSegment save(ScheduledNotificationSegment segment) {
    return jpaScheduledNotificationSegmentRepository.save(segment);
  }

  @Override
  public List<ScheduledNotificationSegment> findActiveSegments() {
    return jpaScheduledNotificationSegmentRepository.findByActiveTrueOrderByNameAsc();
  }

  @Override
  public Optional<ScheduledNotificationSegment> findById(Long id) {
    return jpaScheduledNotificationSegmentRepository.findById(id);
  }
}
