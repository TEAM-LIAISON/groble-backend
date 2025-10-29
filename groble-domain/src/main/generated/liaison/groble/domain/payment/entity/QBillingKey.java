package liaison.groble.domain.payment.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QBillingKey is a Querydsl query type for BillingKey
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QBillingKey extends EntityPathBase<BillingKey> {

    private static final long serialVersionUID = -1552416877L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QBillingKey billingKey1 = new QBillingKey("billingKey1");

    public final liaison.groble.domain.common.entity.QBaseTimeEntity _super = new liaison.groble.domain.common.entity.QBaseTimeEntity(this);

    public final StringPath billingKey = createString("billingKey");

    public final StringPath cardName = createString("cardName");

    public final StringPath cardNumberMasked = createString("cardNumberMasked");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final DateTimePath<java.time.LocalDateTime> lastUsedAt = createDateTime("lastUsedAt", java.time.LocalDateTime.class);

    public final EnumPath<liaison.groble.domain.payment.enums.BillingKeyStatus> status = createEnum("status", liaison.groble.domain.payment.enums.BillingKeyStatus.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final liaison.groble.domain.user.entity.QUser user;

    public QBillingKey(String variable) {
        this(BillingKey.class, forVariable(variable), INITS);
    }

    public QBillingKey(Path<? extends BillingKey> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QBillingKey(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QBillingKey(PathMetadata metadata, PathInits inits) {
        this(BillingKey.class, metadata, inits);
    }

    public QBillingKey(Class<? extends BillingKey> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new liaison.groble.domain.user.entity.QUser(forProperty("user"), inits.get("user")) : null;
    }

}

