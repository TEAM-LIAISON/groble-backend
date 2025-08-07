package liaison.groble.domain.terms.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUserOrderTerms is a Querydsl query type for UserOrderTerms
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserOrderTerms extends EntityPathBase<UserOrderTerms> {

    private static final long serialVersionUID = 391716596L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUserOrderTerms userOrderTerms = new QUserOrderTerms("userOrderTerms");

    public final BooleanPath agreed = createBoolean("agreed");

    public final DateTimePath<java.time.LocalDateTime> agreedAt = createDateTime("agreedAt", java.time.LocalDateTime.class);

    public final StringPath agreedIp = createString("agreedIp");

    public final StringPath agreedUserAgent = createString("agreedUserAgent");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QOrderTerms orderTerms;

    public final liaison.groble.domain.user.entity.QUser user;

    public QUserOrderTerms(String variable) {
        this(UserOrderTerms.class, forVariable(variable), INITS);
    }

    public QUserOrderTerms(Path<? extends UserOrderTerms> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUserOrderTerms(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUserOrderTerms(PathMetadata metadata, PathInits inits) {
        this(UserOrderTerms.class, metadata, inits);
    }

    public QUserOrderTerms(Class<? extends UserOrderTerms> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.orderTerms = inits.isInitialized("orderTerms") ? new QOrderTerms(forProperty("orderTerms")) : null;
        this.user = inits.isInitialized("user") ? new liaison.groble.domain.user.entity.QUser(forProperty("user"), inits.get("user")) : null;
    }

}

