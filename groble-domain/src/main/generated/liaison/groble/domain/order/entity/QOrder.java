package liaison.groble.domain.order.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QOrder is a Querydsl query type for Order
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QOrder extends EntityPathBase<Order> {

    private static final long serialVersionUID = -1001811721L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QOrder order = new QOrder("order1");

    public final liaison.groble.domain.common.entity.QBaseTimeEntity _super = new liaison.groble.domain.common.entity.QBaseTimeEntity(this);

    public final liaison.groble.domain.coupon.entity.QUserCoupon appliedCoupon;

    public final NumberPath<java.math.BigDecimal> couponDiscountAmount = createNumber("couponDiscountAmount", java.math.BigDecimal.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<java.math.BigDecimal> discountAmount = createNumber("discountAmount", java.math.BigDecimal.class);

    public final NumberPath<java.math.BigDecimal> finalAmount = createNumber("finalAmount", java.math.BigDecimal.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath merchantUid = createString("merchantUid");

    public final ListPath<OrderItem, QOrderItem> orderItems = this.<OrderItem, QOrderItem>createList("orderItems", OrderItem.class, QOrderItem.class, PathInits.DIRECT2);

    public final StringPath orderNote = createString("orderNote");

    public final NumberPath<java.math.BigDecimal> originalAmount = createNumber("originalAmount", java.math.BigDecimal.class);

    public final liaison.groble.domain.payment.entity.QPayment payment;

    public final liaison.groble.domain.purchase.entity.QPurchaser purchaser;

    public final EnumPath<Order.OrderStatus> status = createEnum("status", Order.OrderStatus.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final liaison.groble.domain.user.entity.QUser user;

    public final NumberPath<Long> version = createNumber("version", Long.class);

    public QOrder(String variable) {
        this(Order.class, forVariable(variable), INITS);
    }

    public QOrder(Path<? extends Order> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QOrder(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QOrder(PathMetadata metadata, PathInits inits) {
        this(Order.class, metadata, inits);
    }

    public QOrder(Class<? extends Order> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.appliedCoupon = inits.isInitialized("appliedCoupon") ? new liaison.groble.domain.coupon.entity.QUserCoupon(forProperty("appliedCoupon"), inits.get("appliedCoupon")) : null;
        this.payment = inits.isInitialized("payment") ? new liaison.groble.domain.payment.entity.QPayment(forProperty("payment"), inits.get("payment")) : null;
        this.purchaser = inits.isInitialized("purchaser") ? new liaison.groble.domain.purchase.entity.QPurchaser(forProperty("purchaser")) : null;
        this.user = inits.isInitialized("user") ? new liaison.groble.domain.user.entity.QUser(forProperty("user"), inits.get("user")) : null;
    }

}

