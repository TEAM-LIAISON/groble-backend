package liaison.groble.domain.subscription.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSubscription is a Querydsl query type for Subscription
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSubscription extends EntityPathBase<Subscription> {

    private static final long serialVersionUID = 1809546507L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QSubscription subscription = new QSubscription("subscription");

    public final liaison.groble.domain.common.entity.QBaseTimeEntity _super = new liaison.groble.domain.common.entity.QBaseTimeEntity(this);

    public final DateTimePath<java.time.LocalDateTime> activatedAt = createDateTime("activatedAt", java.time.LocalDateTime.class);

    public final StringPath billingKey = createString("billingKey");

    public final DateTimePath<java.time.LocalDateTime> cancelledAt = createDateTime("cancelledAt", java.time.LocalDateTime.class);

    public final liaison.groble.domain.content.entity.QContent content;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final DatePath<java.time.LocalDate> nextBillingDate = createDate("nextBillingDate", java.time.LocalDate.class);

    public final NumberPath<Long> optionId = createNumber("optionId", Long.class);

    public final StringPath optionName = createString("optionName");

    public final liaison.groble.domain.payment.entity.QPayment payment;

    public final NumberPath<java.math.BigDecimal> price = createNumber("price", java.math.BigDecimal.class);

    public final liaison.groble.domain.purchase.entity.QPurchase purchase;

    public final EnumPath<liaison.groble.domain.subscription.enums.SubscriptionStatus> status = createEnum("status", liaison.groble.domain.subscription.enums.SubscriptionStatus.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final liaison.groble.domain.user.entity.QUser user;

    public QSubscription(String variable) {
        this(Subscription.class, forVariable(variable), INITS);
    }

    public QSubscription(Path<? extends Subscription> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QSubscription(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QSubscription(PathMetadata metadata, PathInits inits) {
        this(Subscription.class, metadata, inits);
    }

    public QSubscription(Class<? extends Subscription> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.content = inits.isInitialized("content") ? new liaison.groble.domain.content.entity.QContent(forProperty("content"), inits.get("content")) : null;
        this.payment = inits.isInitialized("payment") ? new liaison.groble.domain.payment.entity.QPayment(forProperty("payment"), inits.get("payment")) : null;
        this.purchase = inits.isInitialized("purchase") ? new liaison.groble.domain.purchase.entity.QPurchase(forProperty("purchase"), inits.get("purchase")) : null;
        this.user = inits.isInitialized("user") ? new liaison.groble.domain.user.entity.QUser(forProperty("user"), inits.get("user")) : null;
    }

}

