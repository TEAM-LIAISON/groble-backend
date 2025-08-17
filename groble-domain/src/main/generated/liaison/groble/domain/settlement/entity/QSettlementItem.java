package liaison.groble.domain.settlement.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSettlementItem is a Querydsl query type for SettlementItem
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSettlementItem extends EntityPathBase<SettlementItem> {

    private static final long serialVersionUID = -2047956906L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QSettlementItem settlementItem = new QSettlementItem("settlementItem");

    public final liaison.groble.domain.common.entity.QBaseTimeEntity _super = new liaison.groble.domain.common.entity.QBaseTimeEntity(this);

    public final NumberPath<java.math.BigDecimal> capturedPgFeeRate = createNumber("capturedPgFeeRate", java.math.BigDecimal.class);

    public final NumberPath<java.math.BigDecimal> capturedPlatformFeeRate = createNumber("capturedPlatformFeeRate", java.math.BigDecimal.class);

    public final NumberPath<java.math.BigDecimal> capturedVatRate = createNumber("capturedVatRate", java.math.BigDecimal.class);

    public final StringPath contentTitle = createString("contentTitle");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<java.math.BigDecimal> feeVat = createNumber("feeVat", java.math.BigDecimal.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isRefunded = createBoolean("isRefunded");

    public final StringPath optionName = createString("optionName");

    public final NumberPath<java.math.BigDecimal> pgFee = createNumber("pgFee", java.math.BigDecimal.class);

    public final NumberPath<java.math.BigDecimal> platformFee = createNumber("platformFee", java.math.BigDecimal.class);

    public final liaison.groble.domain.purchase.entity.QPurchase purchase;

    public final DateTimePath<java.time.LocalDateTime> purchasedAt = createDateTime("purchasedAt", java.time.LocalDateTime.class);

    public final StringPath purchaserName = createString("purchaserName");

    public final DateTimePath<java.time.LocalDateTime> refundedAt = createDateTime("refundedAt", java.time.LocalDateTime.class);

    public final NumberPath<java.math.BigDecimal> salesAmount = createNumber("salesAmount", java.math.BigDecimal.class);

    public final QSettlement settlement;

    public final NumberPath<java.math.BigDecimal> settlementAmount = createNumber("settlementAmount", java.math.BigDecimal.class);

    public final BooleanPath taxInvoiceEligible = createBoolean("taxInvoiceEligible");

    public final ListPath<TaxInvoice, QTaxInvoice> taxInvoices = this.<TaxInvoice, QTaxInvoice>createList("taxInvoices", TaxInvoice.class, QTaxInvoice.class, PathInits.DIRECT2);

    public final NumberPath<java.math.BigDecimal> totalFee = createNumber("totalFee", java.math.BigDecimal.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final NumberPath<Long> version = createNumber("version", Long.class);

    public QSettlementItem(String variable) {
        this(SettlementItem.class, forVariable(variable), INITS);
    }

    public QSettlementItem(Path<? extends SettlementItem> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QSettlementItem(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QSettlementItem(PathMetadata metadata, PathInits inits) {
        this(SettlementItem.class, metadata, inits);
    }

    public QSettlementItem(Class<? extends SettlementItem> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.purchase = inits.isInitialized("purchase") ? new liaison.groble.domain.purchase.entity.QPurchase(forProperty("purchase"), inits.get("purchase")) : null;
        this.settlement = inits.isInitialized("settlement") ? new QSettlement(forProperty("settlement"), inits.get("settlement")) : null;
    }

}

