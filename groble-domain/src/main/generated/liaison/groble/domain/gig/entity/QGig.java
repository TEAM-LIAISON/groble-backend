package liaison.groble.domain.gig.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QGig is a Querydsl query type for Gig
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QGig extends EntityPathBase<Gig> {

    private static final long serialVersionUID = 1309062103L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QGig gig = new QGig("gig");

    public final liaison.groble.domain.common.entity.QBaseEntity _super = new liaison.groble.domain.common.entity.QBaseEntity(this);

    public final QCategory category;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final StringPath createdBy = _super.createdBy;

    //inherited
    public final BooleanPath deleted = _super.deleted;

    public final EnumPath<liaison.groble.domain.gig.enums.GigType> gigType = createEnum("gigType", liaison.groble.domain.gig.enums.GigType.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final ListPath<GigOption, QGigOption> options = this.<GigOption, QGigOption>createList("options", GigOption.class, QGigOption.class, PathInits.DIRECT2);

    public final NumberPath<Integer> saleCount = createNumber("saleCount", Integer.class);

    public final EnumPath<liaison.groble.domain.gig.enums.GigStatus> status = createEnum("status", liaison.groble.domain.gig.enums.GigStatus.class);

    public final StringPath thumbnailUrl = createString("thumbnailUrl");

    public final StringPath title = createString("title");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    //inherited
    public final StringPath updatedBy = _super.updatedBy;

    public final liaison.groble.domain.user.entity.QUser user;

    public QGig(String variable) {
        this(Gig.class, forVariable(variable), INITS);
    }

    public QGig(Path<? extends Gig> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QGig(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QGig(PathMetadata metadata, PathInits inits) {
        this(Gig.class, metadata, inits);
    }

    public QGig(Class<? extends Gig> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.category = inits.isInitialized("category") ? new QCategory(forProperty("category"), inits.get("category")) : null;
        this.user = inits.isInitialized("user") ? new liaison.groble.domain.user.entity.QUser(forProperty("user"), inits.get("user")) : null;
    }

}

