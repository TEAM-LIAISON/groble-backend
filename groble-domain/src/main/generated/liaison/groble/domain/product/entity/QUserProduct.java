package liaison.groble.domain.product.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import javax.annotation.processing.Generated;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.*;
import com.querydsl.core.types.dsl.PathInits;

/** QUserProduct is a Querydsl query type for UserProduct */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserProduct extends EntityPathBase<UserProduct> {

  private static final long serialVersionUID = -758393524L;

  private static final PathInits INITS = PathInits.DIRECT2;

  public static final QUserProduct userProduct = new QUserProduct("userProduct");

  public final liaison.groble.domain.common.entity.QBaseEntity _super =
      new liaison.groble.domain.common.entity.QBaseEntity(this);

  public final NumberPath<Integer> accessCount = createNumber("accessCount", Integer.class);

  // inherited
  public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

  // inherited
  public final StringPath createdBy = _super.createdBy;

  // inherited
  public final BooleanPath deleted = _super.deleted;

  public final StringPath downloadUrlHash = createString("downloadUrlHash");

  public final DateTimePath<java.time.LocalDateTime> expiresAt =
      createDateTime("expiresAt", java.time.LocalDateTime.class);

  public final NumberPath<Long> id = createNumber("id", Long.class);

  public final DateTimePath<java.time.LocalDateTime> lastAccessedAt =
      createDateTime("lastAccessedAt", java.time.LocalDateTime.class);

  public final StringPath optionName = createString("optionName");

  public final liaison.groble.domain.order.entity.QOrder order;

  public final QProduct product;

  public final NumberPath<Long> selectedOptionId = createNumber("selectedOptionId", Long.class);

  public final EnumPath<liaison.groble.domain.payment.entity.Payment.SelectedOptionType>
      selectedOptionType =
          createEnum(
              "selectedOptionType",
              liaison.groble.domain.payment.entity.Payment.SelectedOptionType.class);

  // inherited
  public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

  // inherited
  public final StringPath updatedBy = _super.updatedBy;

  public final liaison.groble.domain.user.entity.QUser user;

  public QUserProduct(String variable) {
    this(UserProduct.class, forVariable(variable), INITS);
  }

  public QUserProduct(Path<? extends UserProduct> path) {
    this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
  }

  public QUserProduct(PathMetadata metadata) {
    this(metadata, PathInits.getFor(metadata, INITS));
  }

  public QUserProduct(PathMetadata metadata, PathInits inits) {
    this(UserProduct.class, metadata, inits);
  }

  public QUserProduct(Class<? extends UserProduct> type, PathMetadata metadata, PathInits inits) {
    super(type, metadata, inits);
    this.order =
        inits.isInitialized("order")
            ? new liaison.groble.domain.order.entity.QOrder(
                forProperty("order"), inits.get("order"))
            : null;
    this.product = inits.isInitialized("product") ? new QProduct(forProperty("product")) : null;
    this.user =
        inits.isInitialized("user")
            ? new liaison.groble.domain.user.entity.QUser(forProperty("user"), inits.get("user"))
            : null;
  }
}
