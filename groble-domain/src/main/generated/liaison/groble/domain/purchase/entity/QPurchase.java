package liaison.groble.domain.purchase.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPurchase is a Querydsl query type for Purchase
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPurchase extends EntityPathBase<Purchase> {

    private static final long serialVersionUID = -1287926317L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPurchase purchase = new QPurchase("purchase");

    public final liaison.groble.domain.common.entity.QBaseTimeEntity _super = new liaison.groble.domain.common.entity.QBaseTimeEntity(this);

    public final DateTimePath<java.time.LocalDateTime> cancelledAt = createDateTime("cancelledAt", java.time.LocalDateTime.class);

    public final StringPath cancelReason = createString("cancelReason");

    public final DateTimePath<java.time.LocalDateTime> confirmedAt = createDateTime("confirmedAt", java.time.LocalDateTime.class);

    public final liaison.groble.domain.content.entity.QContent content;

    public final StringPath couponCode = createString("couponCode");

    public final NumberPath<java.math.BigDecimal> couponDiscountPrice = createNumber("couponDiscountPrice", java.math.BigDecimal.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<java.math.BigDecimal> discountPrice = createNumber("discountPrice", java.math.BigDecimal.class);

    public final NumberPath<java.math.BigDecimal> finalPrice = createNumber("finalPrice", java.math.BigDecimal.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final liaison.groble.domain.order.entity.QOrder order;

    public final NumberPath<java.math.BigDecimal> originalPrice = createNumber("originalPrice", java.math.BigDecimal.class);

    public final liaison.groble.domain.payment.entity.QPayment payment;

    public final DateTimePath<java.time.LocalDateTime> purchasedAt = createDateTime("purchasedAt", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> refundedAt = createDateTime("refundedAt", java.time.LocalDateTime.class);

    public final StringPath refundReason = createString("refundReason");

    public final DateTimePath<java.time.LocalDateTime> refundRequestedAt = createDateTime("refundRequestedAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> selectedOptionId = createNumber("selectedOptionId", Long.class);

    public final StringPath selectedOptionName = createString("selectedOptionName");

    public final EnumPath<Purchase.OptionType> selectedOptionType = createEnum("selectedOptionType", Purchase.OptionType.class);

    public final EnumPath<liaison.groble.domain.purchase.enums.PurchaseStatus> status = createEnum("status", liaison.groble.domain.purchase.enums.PurchaseStatus.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final liaison.groble.domain.coupon.entity.QUserCoupon usedCoupon;

    public final liaison.groble.domain.user.entity.QUser user;

    public QPurchase(String variable) {
        this(Purchase.class, forVariable(variable), INITS);
    }

    public QPurchase(Path<? extends Purchase> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPurchase(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPurchase(PathMetadata metadata, PathInits inits) {
        this(Purchase.class, metadata, inits);
    }

    public QPurchase(Class<? extends Purchase> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.content = inits.isInitialized("content") ? new liaison.groble.domain.content.entity.QContent(forProperty("content"), inits.get("content")) : null;
        this.order = inits.isInitialized("order") ? new liaison.groble.domain.order.entity.QOrder(forProperty("order"), inits.get("order")) : null;
        this.payment = inits.isInitialized("payment") ? new liaison.groble.domain.payment.entity.QPayment(forProperty("payment"), inits.get("payment")) : null;
        this.usedCoupon = inits.isInitialized("usedCoupon") ? new liaison.groble.domain.coupon.entity.QUserCoupon(forProperty("usedCoupon"), inits.get("usedCoupon")) : null;
        this.user = inits.isInitialized("user") ? new liaison.groble.domain.user.entity.QUser(forProperty("user"), inits.get("user")) : null;
    }

}

