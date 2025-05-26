package liaison.groble.domain.payment.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QPayplePayment is a Querydsl query type for PayplePayment
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPayplePayment extends EntityPathBase<PayplePayment> {

    private static final long serialVersionUID = -1282774410L;

    public static final QPayplePayment payplePayment = new QPayplePayment("payplePayment");

    public final liaison.groble.domain.common.entity.QBaseTimeEntity _super = new liaison.groble.domain.common.entity.QBaseTimeEntity(this);

    public final NumberPath<java.math.BigDecimal> amount = createNumber("amount", java.math.BigDecimal.class);

    public final StringPath bankAccount = createString("bankAccount");

    public final StringPath bankName = createString("bankName");

    public final StringPath billingKey = createString("billingKey");

    public final DateTimePath<java.time.LocalDateTime> canceledAt = createDateTime("canceledAt", java.time.LocalDateTime.class);

    public final StringPath cancelReason = createString("cancelReason");

    public final StringPath cardName = createString("cardName");

    public final StringPath cardNumber = createString("cardNumber");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath failReason = createString("failReason");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath orderId = createString("orderId");

    public final StringPath payerId = createString("payerId");

    public final DateTimePath<java.time.LocalDateTime> paymentDate = createDateTime("paymentDate", java.time.LocalDateTime.class);

    public final StringPath payMethod = createString("payMethod");

    public final StringPath productName = createString("productName");

    public final StringPath receiptUrl = createString("receiptUrl");

    public final EnumPath<liaison.groble.domain.payment.enums.PayplePaymentStatus> status = createEnum("status", liaison.groble.domain.payment.enums.PayplePaymentStatus.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public QPayplePayment(String variable) {
        super(PayplePayment.class, forVariable(variable));
    }

    public QPayplePayment(Path<? extends PayplePayment> path) {
        super(path.getType(), path.getMetadata());
    }

    public QPayplePayment(PathMetadata metadata) {
        super(PayplePayment.class, metadata);
    }

}

