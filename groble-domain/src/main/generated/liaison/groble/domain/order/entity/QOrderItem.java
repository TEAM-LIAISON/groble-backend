package liaison.groble.domain.order.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import javax.annotation.processing.Generated;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.*;
import com.querydsl.core.types.dsl.PathInits;

/** QOrderItem is a Querydsl query type for OrderItem */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QOrderItem extends EntityPathBase<OrderItem> {

  private static final long serialVersionUID = 1925000362L;

  private static final PathInits INITS = PathInits.DIRECT2;

  public static final QOrderItem orderItem = new QOrderItem("orderItem");

  public final liaison.groble.domain.common.entity.QBaseEntity _super =
      new liaison.groble.domain.common.entity.QBaseEntity(this);

  public final StringPath contentName = createString("contentName");

  // inherited
  public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

  // inherited
  public final StringPath createdBy = _super.createdBy;

  // inherited
  public final BooleanPath deleted = _super.deleted;

  public final NumberPath<Long> id = createNumber("id", Long.class);

  public final NumberPath<Long> optionId = createNumber("optionId", Long.class);

  public final StringPath optionName = createString("optionName");

  public final EnumPath<OrderItem.OptionType> optionType =
      createEnum("optionType", OrderItem.OptionType.class);

  public final QOrder order;

  public final NumberPath<java.math.BigDecimal> price =
      createNumber("price", java.math.BigDecimal.class);

  public final liaison.groble.domain.product.entity.QProduct product;

  public final NumberPath<Integer> quantity = createNumber("quantity", Integer.class);

  // inherited
  public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

  // inherited
  public final StringPath updatedBy = _super.updatedBy;

  public QOrderItem(String variable) {
    this(OrderItem.class, forVariable(variable), INITS);
  }

  public QOrderItem(Path<? extends OrderItem> path) {
    this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
  }

  public QOrderItem(PathMetadata metadata) {
    this(metadata, PathInits.getFor(metadata, INITS));
  }

  public QOrderItem(PathMetadata metadata, PathInits inits) {
    this(OrderItem.class, metadata, inits);
  }

  public QOrderItem(Class<? extends OrderItem> type, PathMetadata metadata, PathInits inits) {
    super(type, metadata, inits);
    this.order =
        inits.isInitialized("order") ? new QOrder(forProperty("order"), inits.get("order")) : null;
    this.product =
        inits.isInitialized("product")
            ? new liaison.groble.domain.product.entity.QProduct(forProperty("product"))
            : null;
  }
}
