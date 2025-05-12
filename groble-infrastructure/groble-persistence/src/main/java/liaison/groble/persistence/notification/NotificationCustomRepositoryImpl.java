package liaison.groble.persistence.notification;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.querydsl.jpa.impl.JPAQueryFactory;

import liaison.groble.domain.notification.entity.Notification;
import liaison.groble.domain.notification.entity.QNotification;
import liaison.groble.domain.notification.repository.NotificationCustomRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class NotificationCustomRepositoryImpl implements NotificationCustomRepository {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<Notification> getNotificationsByReceiverUser(final Long userId) {
    QNotification qNotification = QNotification.notification;

    return queryFactory
        .selectFrom(qNotification)
        .where(qNotification.user.id.eq(userId))
        .orderBy(qNotification.createdAt.desc())
        .fetch();
  }
}
