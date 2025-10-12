package liaison.groble.domain.notification.scheduled.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QScheduledNotificationSegment is a Querydsl query type for ScheduledNotificationSegment
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QScheduledNotificationSegment extends EntityPathBase<ScheduledNotificationSegment> {

    private static final long serialVersionUID = 1362650262L;

    public static final QScheduledNotificationSegment scheduledNotificationSegment = new QScheduledNotificationSegment("scheduledNotificationSegment");

    public final liaison.groble.domain.common.entity.QBaseTimeEntity _super = new liaison.groble.domain.common.entity.QBaseTimeEntity(this);

    public final BooleanPath active = createBoolean("active");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> createdByAdminId = createNumber("createdByAdminId", Long.class);

    public final StringPath description = createString("description");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public final StringPath segmentPayload = createString("segmentPayload");

    public final EnumPath<liaison.groble.domain.notification.scheduled.enums.ScheduledNotificationSegmentType> segmentType = createEnum("segmentType", liaison.groble.domain.notification.scheduled.enums.ScheduledNotificationSegmentType.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final NumberPath<Long> updatedByAdminId = createNumber("updatedByAdminId", Long.class);

    public final NumberPath<Long> version = createNumber("version", Long.class);

    public QScheduledNotificationSegment(String variable) {
        super(ScheduledNotificationSegment.class, forVariable(variable));
    }

    public QScheduledNotificationSegment(Path<? extends ScheduledNotificationSegment> path) {
        super(path.getType(), path.getMetadata());
    }

    public QScheduledNotificationSegment(PathMetadata metadata) {
        super(ScheduledNotificationSegment.class, metadata);
    }

}

