package liaison.groble.domain.notification.scheduled.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import javax.annotation.processing.Generated;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.*;
import com.querydsl.core.types.dsl.PathInits;

/** QScheduledNotification is a Querydsl query type for ScheduledNotification */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QScheduledNotification extends EntityPathBase<ScheduledNotification> {

  private static final long serialVersionUID = -1323978787L;

  public static final QScheduledNotification scheduledNotification =
      new QScheduledNotification("scheduledNotification");

  public final liaison.groble.domain.common.entity.QBaseTimeEntity _super =
      new liaison.groble.domain.common.entity.QBaseTimeEntity(this);

  public final StringPath bizSenderKey = createString("bizSenderKey");

  public final StringPath bizTemplateCode = createString("bizTemplateCode");

  public final EnumPath<
          liaison.groble.domain.notification.scheduled.enums.ScheduledNotificationChannel>
      channel =
          createEnum(
              "channel",
              liaison.groble.domain.notification.scheduled.enums.ScheduledNotificationChannel
                  .class);

  public final StringPath content = createString("content");

  // inherited
  public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

  public final NumberPath<Long> createdByAdminId = createNumber("createdByAdminId", Long.class);

  public final NumberPath<Long> id = createNumber("id", Long.class);

  public final StringPath repeatCron = createString("repeatCron");

  public final ListPath<ScheduledNotificationRun, QScheduledNotificationRun> runs =
      this.<ScheduledNotificationRun, QScheduledNotificationRun>createList(
          "runs",
          ScheduledNotificationRun.class,
          QScheduledNotificationRun.class,
          PathInits.DIRECT2);

  public final DateTimePath<java.time.LocalDateTime> scheduledAt =
      createDateTime("scheduledAt", java.time.LocalDateTime.class);

  public final StringPath segmentPayload = createString("segmentPayload");

  public final EnumPath<
          liaison.groble.domain.notification.scheduled.enums.ScheduledNotificationSegmentType>
      segmentType =
          createEnum(
              "segmentType",
              liaison.groble.domain.notification.scheduled.enums.ScheduledNotificationSegmentType
                  .class);

  public final EnumPath<
          liaison.groble.domain.notification.scheduled.enums.ScheduledNotificationSendType>
      sendType =
          createEnum(
              "sendType",
              liaison.groble.domain.notification.scheduled.enums.ScheduledNotificationSendType
                  .class);

  public final EnumPath<
          liaison.groble.domain.notification.scheduled.enums.ScheduledNotificationStatus>
      status =
          createEnum(
              "status",
              liaison.groble.domain.notification.scheduled.enums.ScheduledNotificationStatus.class);

  public final StringPath timezone = createString("timezone");

  public final StringPath title = createString("title");

  // inherited
  public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

  public final NumberPath<Long> updatedByAdminId = createNumber("updatedByAdminId", Long.class);

  public final NumberPath<Long> version = createNumber("version", Long.class);

  public QScheduledNotification(String variable) {
    super(ScheduledNotification.class, forVariable(variable));
  }

  public QScheduledNotification(Path<? extends ScheduledNotification> path) {
    super(path.getType(), path.getMetadata());
  }

  public QScheduledNotification(PathMetadata metadata) {
    super(ScheduledNotification.class, metadata);
  }
}
