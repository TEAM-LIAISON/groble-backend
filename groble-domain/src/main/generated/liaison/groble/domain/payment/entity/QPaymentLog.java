package liaison.groble.domain.payment.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPaymentLog is a Querydsl query type for PaymentLog
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPaymentLog extends EntityPathBase<PaymentLog> {

    private static final long serialVersionUID = -773798643L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPaymentLog paymentLog = new QPaymentLog("paymentLog");

    public final liaison.groble.domain.common.entity.QBaseEntity _super = new liaison.groble.domain.common.entity.QBaseEntity(this);

    public final EnumPath<Payment.PaymentStatus> afterStatus = createEnum("afterStatus", Payment.PaymentStatus.class);

    public final EnumPath<Payment.PaymentStatus> beforeStatus = createEnum("beforeStatus", Payment.PaymentStatus.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final StringPath createdBy = _super.createdBy;

    //inherited
    public final BooleanPath deleted = _super.deleted;

    public final StringPath description = createString("description");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath ipAddress = createString("ipAddress");

    public final QPayment payment;

    public final MapPath<String, Object, SimplePath<Object>> requestData = this.<String, Object, SimplePath<Object>>createMap("requestData", String.class, Object.class, SimplePath.class);

    public final MapPath<String, Object, SimplePath<Object>> responseData = this.<String, Object, SimplePath<Object>>createMap("responseData", String.class, Object.class, SimplePath.class);

    public final EnumPath<liaison.groble.domain.payment.enums.PaymentLogType> type = createEnum("type", liaison.groble.domain.payment.enums.PaymentLogType.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    //inherited
    public final StringPath updatedBy = _super.updatedBy;

    public final StringPath userAgent = createString("userAgent");

    public QPaymentLog(String variable) {
        this(PaymentLog.class, forVariable(variable), INITS);
    }

    public QPaymentLog(Path<? extends PaymentLog> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPaymentLog(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPaymentLog(PathMetadata metadata, PathInits inits) {
        this(PaymentLog.class, metadata, inits);
    }

    public QPaymentLog(Class<? extends PaymentLog> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.payment = inits.isInitialized("payment") ? new QPayment(forProperty("payment"), inits.get("payment")) : null;
    }

}

