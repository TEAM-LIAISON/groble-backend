package liaison.groble.domain.settlement.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import javax.annotation.processing.Generated;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.*;
import com.querydsl.core.types.dsl.PathInits;

/** QTaxInvoice is a Querydsl query type for TaxInvoice */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QTaxInvoice extends EntityPathBase<TaxInvoice> {

  private static final long serialVersionUID = 753823356L;

  private static final PathInits INITS = PathInits.DIRECT2;

  public static final QTaxInvoice taxInvoice = new QTaxInvoice("taxInvoice");

  public final NumberPath<Long> id = createNumber("id", Long.class);

  public final StringPath invoiceNumber = createString("invoiceNumber");

  public final EnumPath<TaxInvoice.InvoiceType> invoiceType =
      createEnum("invoiceType", TaxInvoice.InvoiceType.class);

  public final StringPath invoiceUrl = createString("invoiceUrl");

  public final DatePath<java.time.LocalDate> issuedDate =
      createDate("issuedDate", java.time.LocalDate.class);

  public final StringPath note = createString("note");

  public final QSettlement settlement;

  public final QSettlementItem settlementItem;

  public final NumberPath<Integer> settlementRound = createNumber("settlementRound", Integer.class);

  public final StringPath settlementType = createString("settlementType");

  public final EnumPath<TaxInvoice.InvoiceStatus> status =
      createEnum("status", TaxInvoice.InvoiceStatus.class);

  public final NumberPath<java.math.BigDecimal> supplyAmount =
      createNumber("supplyAmount", java.math.BigDecimal.class);

  public final NumberPath<java.math.BigDecimal> totalAmount =
      createNumber("totalAmount", java.math.BigDecimal.class);

  public final NumberPath<java.math.BigDecimal> vatAmount =
      createNumber("vatAmount", java.math.BigDecimal.class);

  public QTaxInvoice(String variable) {
    this(TaxInvoice.class, forVariable(variable), INITS);
  }

  public QTaxInvoice(Path<? extends TaxInvoice> path) {
    this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
  }

  public QTaxInvoice(PathMetadata metadata) {
    this(metadata, PathInits.getFor(metadata, INITS));
  }

  public QTaxInvoice(PathMetadata metadata, PathInits inits) {
    this(TaxInvoice.class, metadata, inits);
  }

  public QTaxInvoice(Class<? extends TaxInvoice> type, PathMetadata metadata, PathInits inits) {
    super(type, metadata, inits);
    this.settlement =
        inits.isInitialized("settlement")
            ? new QSettlement(forProperty("settlement"), inits.get("settlement"))
            : null;
    this.settlementItem =
        inits.isInitialized("settlementItem")
            ? new QSettlementItem(forProperty("settlementItem"), inits.get("settlementItem"))
            : null;
  }
}
