package liaison.groble.domain.scrap.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QContentScrap is a Querydsl query type for ContentScrap
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QContentScrap extends EntityPathBase<ContentScrap> {

    private static final long serialVersionUID = -230826478L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QContentScrap contentScrap = new QContentScrap("contentScrap");

    public final liaison.groble.domain.common.entity.QBaseTimeEntity _super = new liaison.groble.domain.common.entity.QBaseTimeEntity(this);

    public final liaison.groble.domain.content.entity.QContent content;

    //inherited
    public final DateTimePath<java.time.Instant> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final DateTimePath<java.time.Instant> modifiedAt = _super.modifiedAt;

    public final liaison.groble.domain.user.entity.QUser user;

    public QContentScrap(String variable) {
        this(ContentScrap.class, forVariable(variable), INITS);
    }

    public QContentScrap(Path<? extends ContentScrap> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QContentScrap(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QContentScrap(PathMetadata metadata, PathInits inits) {
        this(ContentScrap.class, metadata, inits);
    }

    public QContentScrap(Class<? extends ContentScrap> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.content = inits.isInitialized("content") ? new liaison.groble.domain.content.entity.QContent(forProperty("content"), inits.get("content")) : null;
        this.user = inits.isInitialized("user") ? new liaison.groble.domain.user.entity.QUser(forProperty("user"), inits.get("user")) : null;
    }

}

