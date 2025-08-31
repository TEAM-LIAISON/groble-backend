package liaison.groble.domain.terms.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QGuestUserOrderTerms is a Querydsl query type for GuestUserOrderTerms
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QGuestUserOrderTerms extends EntityPathBase<GuestUserOrderTerms> {

    private static final long serialVersionUID = 1699932428L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QGuestUserOrderTerms guestUserOrderTerms = new QGuestUserOrderTerms("guestUserOrderTerms");

    public final BooleanPath agreed = createBoolean("agreed");

    public final DateTimePath<java.time.LocalDateTime> agreedAt = createDateTime("agreedAt", java.time.LocalDateTime.class);

    public final StringPath agreedIp = createString("agreedIp");

    public final StringPath agreedUserAgent = createString("agreedUserAgent");

    public final liaison.groble.domain.guest.entity.QGuestUser guestUser;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QOrderTerms orderTerms;

    public QGuestUserOrderTerms(String variable) {
        this(GuestUserOrderTerms.class, forVariable(variable), INITS);
    }

    public QGuestUserOrderTerms(Path<? extends GuestUserOrderTerms> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QGuestUserOrderTerms(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QGuestUserOrderTerms(PathMetadata metadata, PathInits inits) {
        this(GuestUserOrderTerms.class, metadata, inits);
    }

    public QGuestUserOrderTerms(Class<? extends GuestUserOrderTerms> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.guestUser = inits.isInitialized("guestUser") ? new liaison.groble.domain.guest.entity.QGuestUser(forProperty("guestUser")) : null;
        this.orderTerms = inits.isInitialized("orderTerms") ? new QOrderTerms(forProperty("orderTerms")) : null;
    }

}

