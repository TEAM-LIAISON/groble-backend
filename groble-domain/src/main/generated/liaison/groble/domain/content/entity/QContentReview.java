package liaison.groble.domain.content.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QContentReview is a Querydsl query type for ContentReview
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QContentReview extends EntityPathBase<ContentReview> {

    private static final long serialVersionUID = -847853425L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QContentReview contentReview = new QContentReview("contentReview");

    public final liaison.groble.domain.common.entity.QBaseTimeEntity _super = new liaison.groble.domain.common.entity.QBaseTimeEntity(this);

    public final QContent content;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isDeleted = createBoolean("isDeleted");

    public final NumberPath<java.math.BigDecimal> rating = createNumber("rating", java.math.BigDecimal.class);

    public final StringPath reviewContent = createString("reviewContent");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final liaison.groble.domain.user.entity.QUser user;

    public QContentReview(String variable) {
        this(ContentReview.class, forVariable(variable), INITS);
    }

    public QContentReview(Path<? extends ContentReview> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QContentReview(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QContentReview(PathMetadata metadata, PathInits inits) {
        this(ContentReview.class, metadata, inits);
    }

    public QContentReview(Class<? extends ContentReview> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.content = inits.isInitialized("content") ? new QContent(forProperty("content"), inits.get("content")) : null;
        this.user = inits.isInitialized("user") ? new liaison.groble.domain.user.entity.QUser(forProperty("user"), inits.get("user")) : null;
    }

}

