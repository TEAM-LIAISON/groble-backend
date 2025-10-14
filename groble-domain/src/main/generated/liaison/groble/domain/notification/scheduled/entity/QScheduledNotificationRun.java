package liaison.groble.domain.notification.scheduled.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QScheduledNotificationRun is a Querydsl query type for ScheduledNotificationRun
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QScheduledNotificationRun extends EntityPathBase<ScheduledNotificationRun> {

    private static final long serialVersionUID = -1967281810L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QScheduledNotificationRun scheduledNotificationRun = new QScheduledNotificationRun("scheduledNotificationRun");

    public final liaison.groble.domain.common.entity.QBaseTimeEntity _super = new liaison.groble.domain.common.entity.QBaseTimeEntity(this);

    public final DateTimePath<java.time.LocalDateTime> completedAt = createDateTime("completedAt", java.time.LocalDateTime.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath errorMessage = createString("errorMessage");

    public final DateTimePath<java.time.LocalDateTime> executionTime = createDateTime("executionTime", java.time.LocalDateTime.class);

    public final NumberPath<Integer> failCount = createNumber("failCount", Integer.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Integer> retryCount = createNumber("retryCount", Integer.class);

    public final QScheduledNotification scheduledNotification;

    public final DateTimePath<java.time.LocalDateTime> startedAt = createDateTime("startedAt", java.time.LocalDateTime.class);

    public final EnumPath<liaison.groble.domain.notification.scheduled.enums.ScheduledNotificationRunStatus> status = createEnum("status", liaison.groble.domain.notification.scheduled.enums.ScheduledNotificationRunStatus.class);

    public final NumberPath<Integer> successCount = createNumber("successCount", Integer.class);

    public final NumberPath<Integer> totalTargets = createNumber("totalTargets", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final NumberPath<Long> version = createNumber("version", Long.class);

    public QScheduledNotificationRun(String variable) {
        this(ScheduledNotificationRun.class, forVariable(variable), INITS);
    }

    public QScheduledNotificationRun(Path<? extends ScheduledNotificationRun> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QScheduledNotificationRun(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QScheduledNotificationRun(PathMetadata metadata, PathInits inits) {
        this(ScheduledNotificationRun.class, metadata, inits);
    }

    public QScheduledNotificationRun(Class<? extends ScheduledNotificationRun> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.scheduledNotification = inits.isInitialized("scheduledNotification") ? new QScheduledNotification(forProperty("scheduledNotification")) : null;
    }

}

