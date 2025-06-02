package liaison.groble.domain.coupon.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCouponTemplate is a Querydsl query type for CouponTemplate
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCouponTemplate extends EntityPathBase<CouponTemplate> {

    private static final long serialVersionUID = 1966722167L;

    public static final QCouponTemplate couponTemplate = new QCouponTemplate("couponTemplate");

    public final liaison.groble.domain.common.entity.QBaseTimeEntity _super = new liaison.groble.domain.common.entity.QBaseTimeEntity(this);

    public final EnumPath<liaison.groble.domain.coupon.enums.CouponType> couponType = createEnum("couponType", liaison.groble.domain.coupon.enums.CouponType.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Integer> currentUsageCount = createNumber("currentUsageCount", Integer.class);

    public final StringPath description = createString("description");

    public final NumberPath<java.math.BigDecimal> discountValue = createNumber("discountValue", java.math.BigDecimal.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isActive = createBoolean("isActive");

    public final NumberPath<java.math.BigDecimal> maxDiscountPrice = createNumber("maxDiscountPrice", java.math.BigDecimal.class);

    public final NumberPath<java.math.BigDecimal> minOrderPrice = createNumber("minOrderPrice", java.math.BigDecimal.class);

    public final StringPath name = createString("name");

    public final NumberPath<Integer> totalUsageLimit = createNumber("totalUsageLimit", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final NumberPath<Integer> usageLimitPerUser = createNumber("usageLimitPerUser", Integer.class);

    public final ListPath<UserCoupon, QUserCoupon> userCoupons = this.<UserCoupon, QUserCoupon>createList("userCoupons", UserCoupon.class, QUserCoupon.class, PathInits.DIRECT2);

    public final DateTimePath<java.time.LocalDateTime> validFrom = createDateTime("validFrom", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> validUntil = createDateTime("validUntil", java.time.LocalDateTime.class);

    public QCouponTemplate(String variable) {
        super(CouponTemplate.class, forVariable(variable));
    }

    public QCouponTemplate(Path<? extends CouponTemplate> path) {
        super(path.getType(), path.getMetadata());
    }

    public QCouponTemplate(PathMetadata metadata) {
        super(CouponTemplate.class, metadata);
    }

}

