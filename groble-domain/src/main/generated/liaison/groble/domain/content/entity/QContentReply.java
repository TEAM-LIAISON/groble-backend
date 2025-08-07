package liaison.groble.domain.content.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QContentReply is a Querydsl query type for ContentReply
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QContentReply extends EntityPathBase<ContentReply> {

    private static final long serialVersionUID = -1828471085L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QContentReply contentReply = new QContentReply("contentReply");

    public final liaison.groble.domain.common.entity.QBaseTimeEntity _super = new liaison.groble.domain.common.entity.QBaseTimeEntity(this);

    public final QContentReview contentReview;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isDeleted = createBoolean("isDeleted");

    public final StringPath replyContent = createString("replyContent");

    public final liaison.groble.domain.user.entity.QUser seller;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QContentReply(String variable) {
        this(ContentReply.class, forVariable(variable), INITS);
    }

    public QContentReply(Path<? extends ContentReply> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QContentReply(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QContentReply(PathMetadata metadata, PathInits inits) {
        this(ContentReply.class, metadata, inits);
    }

    public QContentReply(Class<? extends ContentReply> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.contentReview = inits.isInitialized("contentReview") ? new QContentReview(forProperty("contentReview"), inits.get("contentReview")) : null;
        this.seller = inits.isInitialized("seller") ? new liaison.groble.domain.user.entity.QUser(forProperty("seller"), inits.get("seller")) : null;
    }

}

