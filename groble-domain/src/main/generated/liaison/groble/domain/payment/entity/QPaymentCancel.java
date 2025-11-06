package liaison.groble.domain.payment.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import javax.annotation.processing.Generated;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.*;
import com.querydsl.core.types.dsl.PathInits;

/** QPaymentCancel is a Querydsl query type for PaymentCancel */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPaymentCancel extends EntityPathBase<PaymentCancel> {

  private static final long serialVersionUID = -1416180719L;

  private static final PathInits INITS = PathInits.DIRECT2;

  public static final QPaymentCancel paymentCancel = new QPaymentCancel("paymentCancel");

  public final liaison.groble.domain.common.entity.QBaseTimeEntity _super =
      new liaison.groble.domain.common.entity.QBaseTimeEntity(this);

  public final StringPath cancelKey = createString("cancelKey");

  public final DateTimePath<java.time.LocalDateTime> cancelledAt =
      createDateTime("cancelledAt", java.time.LocalDateTime.class);

  // inherited
  public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

  public final NumberPath<Long> id = createNumber("id", Long.class);

  public final MapPath<String, Object, SimplePath<Object>> metaData =
      this.<String, Object, SimplePath<Object>>createMap(
          "metaData", String.class, Object.class, SimplePath.class);

  public final QPayment payment;

  public final NumberPath<java.math.BigDecimal> price =
      createNumber("price", java.math.BigDecimal.class);

  public final StringPath reason = createString("reason");

  public final EnumPath<liaison.groble.domain.payment.enums.PaymentCancelStatus> status =
      createEnum("status", liaison.groble.domain.payment.enums.PaymentCancelStatus.class);

  public final StringPath taxFreePrice = createString("taxFreePrice");

  // inherited
  public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

  public QPaymentCancel(String variable) {
    this(PaymentCancel.class, forVariable(variable), INITS);
  }

  public QPaymentCancel(Path<? extends PaymentCancel> path) {
    this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
  }

  public QPaymentCancel(PathMetadata metadata) {
    this(metadata, PathInits.getFor(metadata, INITS));
  }

  public QPaymentCancel(PathMetadata metadata, PathInits inits) {
    this(PaymentCancel.class, metadata, inits);
  }

  public QPaymentCancel(
      Class<? extends PaymentCancel> type, PathMetadata metadata, PathInits inits) {
    super(type, metadata, inits);
    this.payment =
        inits.isInitialized("payment")
            ? new QPayment(forProperty("payment"), inits.get("payment"))
            : null;
  }
}
