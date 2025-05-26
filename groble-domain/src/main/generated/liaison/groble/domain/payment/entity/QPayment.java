package liaison.groble.domain.payment.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPayment is a Querydsl query type for Payment
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPayment extends EntityPathBase<Payment> {

    private static final long serialVersionUID = -35924297L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPayment payment = new QPayment("payment");

    public final liaison.groble.domain.common.entity.QBaseTimeEntity _super = new liaison.groble.domain.common.entity.QBaseTimeEntity(this);

    public final NumberPath<java.math.BigDecimal> amount = createNumber("amount", java.math.BigDecimal.class);

    public final DateTimePath<java.time.LocalDateTime> cancelAt = createDateTime("cancelAt", java.time.LocalDateTime.class);

    public final ListPath<PaymentCancel, QPaymentCancel> cancellations = this.<PaymentCancel, QPaymentCancel>createList("cancellations", PaymentCancel.class, QPaymentCancel.class, PathInits.DIRECT2);

    public final StringPath cancelReason = createString("cancelReason");

    public final StringPath cardAcquirerCode = createString("cardAcquirerCode");

    public final StringPath cardAcquirerName = createString("cardAcquirerName");

    public final StringPath cardExpiryMonth = createString("cardExpiryMonth");

    public final StringPath cardExpiryYear = createString("cardExpiryYear");

    public final StringPath cardInfo = createString("cardInfo");

    public final StringPath cardInstallmentPlanMonths = createString("cardInstallmentPlanMonths");

    public final StringPath cardIssuerCode = createString("cardIssuerCode");

    public final StringPath cardIssuerName = createString("cardIssuerName");

    public final StringPath cardNumber = createString("cardNumber");

    public final BooleanPath cashReceipt = createBoolean("cashReceipt");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath customerEmail = createString("customerEmail");

    public final StringPath customerMobilePhone = createString("customerMobilePhone");

    public final StringPath customerName = createString("customerName");

    public final BooleanPath escrow = createBoolean("escrow");

    public final StringPath failReason = createString("failReason");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final ListPath<PaymentLog, QPaymentLog> logs = this.<PaymentLog, QPaymentLog>createList("logs", PaymentLog.class, QPaymentLog.class, PathInits.DIRECT2);

    public final StringPath merchantUid = createString("merchantUid");

    public final MapPath<String, Object, SimplePath<Object>> metaData = this.<String, Object, SimplePath<Object>>createMap("metaData", String.class, Object.class, SimplePath.class);

    public final StringPath methodDetail = createString("methodDetail");

    public final liaison.groble.domain.order.entity.QOrder order;

    public final DateTimePath<java.time.LocalDateTime> paidAt = createDateTime("paidAt", java.time.LocalDateTime.class);

    public final StringPath paymentKey = createString("paymentKey");

    public final EnumPath<Payment.PaymentMethod> paymentMethod = createEnum("paymentMethod", Payment.PaymentMethod.class);

    public final EnumPath<Payment.PgProvider> pgProvider = createEnum("pgProvider", Payment.PgProvider.class);

    public final StringPath pgTid = createString("pgTid");

    public final StringPath receiptUrl = createString("receiptUrl");

    public final MapPath<String, Object, SimplePath<Object>> requestParams = this.<String, Object, SimplePath<Object>>createMap("requestParams", String.class, Object.class, SimplePath.class);

    public final MapPath<String, Object, SimplePath<Object>> responseParams = this.<String, Object, SimplePath<Object>>createMap("responseParams", String.class, Object.class, SimplePath.class);

    public final NumberPath<Long> selectedOptionId = createNumber("selectedOptionId", Long.class);

    public final EnumPath<Payment.SelectedOptionType> selectedOptionType = createEnum("selectedOptionType", Payment.SelectedOptionType.class);

    public final EnumPath<Payment.PaymentStatus> status = createEnum("status", Payment.PaymentStatus.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final NumberPath<Long> version = createNumber("version", Long.class);

    public final StringPath virtualAccountBankCode = createString("virtualAccountBankCode");

    public final StringPath virtualAccountBankName = createString("virtualAccountBankName");

    public final DateTimePath<java.time.LocalDateTime> virtualAccountExpiryDate = createDateTime("virtualAccountExpiryDate", java.time.LocalDateTime.class);

    public final StringPath virtualAccountNumber = createString("virtualAccountNumber");

    public QPayment(String variable) {
        this(Payment.class, forVariable(variable), INITS);
    }

    public QPayment(Path<? extends Payment> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPayment(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPayment(PathMetadata metadata, PathInits inits) {
        this(Payment.class, metadata, inits);
    }

    public QPayment(Class<? extends Payment> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.order = inits.isInitialized("order") ? new liaison.groble.domain.order.entity.QOrder(forProperty("order"), inits.get("order")) : null;
    }

}

