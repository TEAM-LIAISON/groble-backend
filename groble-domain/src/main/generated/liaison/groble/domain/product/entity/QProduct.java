package liaison.groble.domain.product.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import javax.annotation.processing.Generated;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.*;

/** QProduct is a Querydsl query type for Product */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QProduct extends EntityPathBase<Product> {

  private static final long serialVersionUID = 2063042455L;

  public static final QProduct product = new QProduct("product");

  public final liaison.groble.domain.common.entity.QBaseTimeEntity _super =
      new liaison.groble.domain.common.entity.QBaseTimeEntity(this);

  public final StringPath contentName = createString("contentName");

  // inherited
  public final DateTimePath<java.time.Instant> createdAt = _super.createdAt;

  public final BooleanPath deleted = createBoolean("deleted");

  public final NumberPath<Long> id = createNumber("id", Long.class);

  // inherited
  public final DateTimePath<java.time.Instant> modifiedAt = _super.modifiedAt;

  public final EnumPath<liaison.groble.domain.product.enums.ProductStatus> status =
      createEnum("status", liaison.groble.domain.product.enums.ProductStatus.class);

  public QProduct(String variable) {
    super(Product.class, forVariable(variable));
  }

  public QProduct(Path<? extends Product> path) {
    super(path.getType(), path.getMetadata());
  }

  public QProduct(PathMetadata metadata) {
    super(Product.class, metadata);
  }
}
