package liaison.groble.domain.coupon.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCouponUsageHistory is a Querydsl query type for CouponUsageHistory
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCouponUsageHistory extends EntityPathBase<CouponUsageHistory> {

    private static final long serialVersionUID = -521318096L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QCouponUsageHistory couponUsageHistory = new QCouponUsageHistory("couponUsageHistory");

    public final liaison.groble.domain.common.entity.QBaseEntity _super = new liaison.groble.domain.common.entity.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final StringPath createdBy = _super.createdBy;

    //inherited
    public final BooleanPath deleted = _super.deleted;

    public final NumberPath<java.math.BigDecimal> discountAmount = createNumber("discountAmount", java.math.BigDecimal.class);

    public final NumberPath<java.math.BigDecimal> finalAmount = createNumber("finalAmount", java.math.BigDecimal.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final liaison.groble.domain.order.entity.QOrder order;

    public final NumberPath<java.math.BigDecimal> originalAmount = createNumber("originalAmount", java.math.BigDecimal.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    //inherited
    public final StringPath updatedBy = _super.updatedBy;

    public final QUserCoupon userCoupon;

    public QCouponUsageHistory(String variable) {
        this(CouponUsageHistory.class, forVariable(variable), INITS);
    }

    public QCouponUsageHistory(Path<? extends CouponUsageHistory> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QCouponUsageHistory(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QCouponUsageHistory(PathMetadata metadata, PathInits inits) {
        this(CouponUsageHistory.class, metadata, inits);
    }

    public QCouponUsageHistory(Class<? extends CouponUsageHistory> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.order = inits.isInitialized("order") ? new liaison.groble.domain.order.entity.QOrder(forProperty("order"), inits.get("order")) : null;
        this.userCoupon = inits.isInitialized("userCoupon") ? new QUserCoupon(forProperty("userCoupon"), inits.get("userCoupon")) : null;
    }

}

