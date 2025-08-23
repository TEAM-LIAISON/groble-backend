package liaison.groble.domain.dashboard.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QContentViewLog is a Querydsl query type for ContentViewLog
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QContentViewLog extends EntityPathBase<ContentViewLog> {

    private static final long serialVersionUID = 172291299L;

    public static final QContentViewLog contentViewLog = new QContentViewLog("contentViewLog");

    public final NumberPath<Long> contentId = createNumber("contentId", Long.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath userAgent = createString("userAgent");

    public final DateTimePath<java.time.LocalDateTime> viewedAt = createDateTime("viewedAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> viewerId = createNumber("viewerId", Long.class);

    public final StringPath viewerIp = createString("viewerIp");

    public final StringPath visitorHash = createString("visitorHash");

    public QContentViewLog(String variable) {
        super(ContentViewLog.class, forVariable(variable));
    }

    public QContentViewLog(Path<? extends ContentViewLog> path) {
        super(path.getType(), path.getMetadata());
    }

    public QContentViewLog(PathMetadata metadata) {
        super(ContentViewLog.class, metadata);
    }

}

