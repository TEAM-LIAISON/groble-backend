package liaison.groble.domain.settlement.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import javax.annotation.processing.Generated;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.*;
import com.querydsl.core.types.dsl.PathInits;

/** QSettlement is a Querydsl query type for Settlement */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSettlement extends EntityPathBase<Settlement> {

  private static final long serialVersionUID = -930889053L;

  private static final PathInits INITS = PathInits.DIRECT2;

  public static final QSettlement settlement = new QSettlement("settlement");

  public final liaison.groble.domain.common.entity.QBaseTimeEntity _super =
      new liaison.groble.domain.common.entity.QBaseTimeEntity(this);

  public final StringPath accountHolder = createString("accountHolder");

  public final StringPath accountNumber = createString("accountNumber");

  public final StringPath bankName = createString("bankName");

  // inherited
  public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

  public final NumberPath<java.math.BigDecimal> feeVat =
      createNumber("feeVat", java.math.BigDecimal.class);

  public final NumberPath<java.math.BigDecimal> feeVatDisplay =
      createNumber("feeVatDisplay", java.math.BigDecimal.class);

  public final NumberPath<Long> id = createNumber("id", Long.class);

  public final DateTimePath<java.time.LocalDateTime> paypleAccountVerificationAt =
      createDateTime("paypleAccountVerificationAt", java.time.LocalDateTime.class);

  public final StringPath paypleApiTranDtm = createString("paypleApiTranDtm");

  public final StringPath paypleApiTranId = createString("paypleApiTranId");

  public final StringPath paypleBankCodeStd = createString("paypleBankCodeStd");

  public final StringPath paypleBankCodeSub = createString("paypleBankCodeSub");

  public final StringPath paypleBankRspCode = createString("paypleBankRspCode");

  public final StringPath paypleBankRspMsg = createString("paypleBankRspMsg");

  public final StringPath paypleBankTranDate = createString("paypleBankTranDate");

  public final StringPath paypleBankTranId = createString("paypleBankTranId");

  public final StringPath paypleBillingTranId = createString("paypleBillingTranId");

  public final NumberPath<java.math.BigDecimal> pgFee =
      createNumber("pgFee", java.math.BigDecimal.class);

  public final NumberPath<java.math.BigDecimal> pgFeeDisplay =
      createNumber("pgFeeDisplay", java.math.BigDecimal.class);

  public final NumberPath<java.math.BigDecimal> pgFeeRate =
      createNumber("pgFeeRate", java.math.BigDecimal.class);

  public final NumberPath<java.math.BigDecimal> pgFeeRateBaseline =
      createNumber("pgFeeRateBaseline", java.math.BigDecimal.class);

  public final NumberPath<java.math.BigDecimal> pgFeeRateDisplay =
      createNumber("pgFeeRateDisplay", java.math.BigDecimal.class);

  public final NumberPath<java.math.BigDecimal> pgFeeRefundExpected =
      createNumber("pgFeeRefundExpected", java.math.BigDecimal.class);

  public final NumberPath<java.math.BigDecimal> platformFee =
      createNumber("platformFee", java.math.BigDecimal.class);

  public final NumberPath<java.math.BigDecimal> platformFeeDisplay =
      createNumber("platformFeeDisplay", java.math.BigDecimal.class);

  public final NumberPath<java.math.BigDecimal> platformFeeForgone =
      createNumber("platformFeeForgone", java.math.BigDecimal.class);

  public final NumberPath<java.math.BigDecimal> platformFeeRate =
      createNumber("platformFeeRate", java.math.BigDecimal.class);

  public final NumberPath<java.math.BigDecimal> platformFeeRateBaseline =
      createNumber("platformFeeRateBaseline", java.math.BigDecimal.class);

  public final NumberPath<java.math.BigDecimal> platformFeeRateDisplay =
      createNumber("platformFeeRateDisplay", java.math.BigDecimal.class);

  public final NumberPath<Integer> refundCount = createNumber("refundCount", Integer.class);

  public final DatePath<java.time.LocalDate> scheduledSettlementDate =
      createDate("scheduledSettlementDate", java.time.LocalDate.class);

  public final DateTimePath<java.time.LocalDateTime> settledAt =
      createDateTime("settledAt", java.time.LocalDateTime.class);

  public final NumberPath<java.math.BigDecimal> settlementAmount =
      createNumber("settlementAmount", java.math.BigDecimal.class);

  public final NumberPath<java.math.BigDecimal> settlementAmountDisplay =
      createNumber("settlementAmountDisplay", java.math.BigDecimal.class);

  public final EnumPath<liaison.groble.domain.settlement.enums.SettlementCycle> settlementCycle =
      createEnum("settlementCycle", liaison.groble.domain.settlement.enums.SettlementCycle.class);

  public final DatePath<java.time.LocalDate> settlementEndDate =
      createDate("settlementEndDate", java.time.LocalDate.class);

  public final ListPath<SettlementItem, QSettlementItem> settlementItems =
      this.<SettlementItem, QSettlementItem>createList(
          "settlementItems", SettlementItem.class, QSettlementItem.class, PathInits.DIRECT2);

  public final StringPath settlementNote = createString("settlementNote");

  public final NumberPath<Integer> settlementRound = createNumber("settlementRound", Integer.class);

  public final DatePath<java.time.LocalDate> settlementStartDate =
      createDate("settlementStartDate", java.time.LocalDate.class);

  public final EnumPath<liaison.groble.domain.settlement.enums.SettlementType> settlementType =
      createEnum("settlementType", liaison.groble.domain.settlement.enums.SettlementType.class);

  public final EnumPath<Settlement.SettlementStatus> status =
      createEnum("status", Settlement.SettlementStatus.class);

  public final BooleanPath taxInvoiceEligible = createBoolean("taxInvoiceEligible");

  public final ListPath<TaxInvoice, QTaxInvoice> taxInvoices =
      this.<TaxInvoice, QTaxInvoice>createList(
          "taxInvoices", TaxInvoice.class, QTaxInvoice.class, PathInits.DIRECT2);

  public final NumberPath<java.math.BigDecimal> totalFee =
      createNumber("totalFee", java.math.BigDecimal.class);

  public final NumberPath<java.math.BigDecimal> totalFeeDisplay =
      createNumber("totalFeeDisplay", java.math.BigDecimal.class);

  public final NumberPath<java.math.BigDecimal> totalRefundAmount =
      createNumber("totalRefundAmount", java.math.BigDecimal.class);

  public final NumberPath<java.math.BigDecimal> totalSalesAmount =
      createNumber("totalSalesAmount", java.math.BigDecimal.class);

  // inherited
  public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

  public final liaison.groble.domain.user.entity.QUser user;

  public final NumberPath<java.math.BigDecimal> vatRate =
      createNumber("vatRate", java.math.BigDecimal.class);

  public final NumberPath<Long> version = createNumber("version", Long.class);

  public QSettlement(String variable) {
    this(Settlement.class, forVariable(variable), INITS);
  }

  public QSettlement(Path<? extends Settlement> path) {
    this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
  }

  public QSettlement(PathMetadata metadata) {
    this(metadata, PathInits.getFor(metadata, INITS));
  }

  public QSettlement(PathMetadata metadata, PathInits inits) {
    this(Settlement.class, metadata, inits);
  }

  public QSettlement(Class<? extends Settlement> type, PathMetadata metadata, PathInits inits) {
    super(type, metadata, inits);
    this.user =
        inits.isInitialized("user")
            ? new liaison.groble.domain.user.entity.QUser(forProperty("user"), inits.get("user"))
            : null;
  }
}
