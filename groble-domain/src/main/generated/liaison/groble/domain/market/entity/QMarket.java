package liaison.groble.domain.market.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import javax.annotation.processing.Generated;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.*;
import com.querydsl.core.types.dsl.PathInits;

/** QMarket is a Querydsl query type for Market */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMarket extends EntityPathBase<Market> {

  private static final long serialVersionUID = 1779327625L;

  private static final PathInits INITS = PathInits.DIRECT2;

  public static final QMarket market = new QMarket("market");

  public final liaison.groble.domain.common.entity.QBaseTimeEntity _super =
      new liaison.groble.domain.common.entity.QBaseTimeEntity(this);

  // inherited
  public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

  public final NumberPath<Long> id = createNumber("id", Long.class);

  public final StringPath marketLinkUrl = createString("marketLinkUrl");

  public final StringPath marketName = createString("marketName");

  // inherited
  public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

  public final liaison.groble.domain.user.entity.QUser user;

  public QMarket(String variable) {
    this(Market.class, forVariable(variable), INITS);
  }

  public QMarket(Path<? extends Market> path) {
    this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
  }

  public QMarket(PathMetadata metadata) {
    this(metadata, PathInits.getFor(metadata, INITS));
  }

  public QMarket(PathMetadata metadata, PathInits inits) {
    this(Market.class, metadata, inits);
  }

  public QMarket(Class<? extends Market> type, PathMetadata metadata, PathInits inits) {
    super(type, metadata, inits);
    this.user =
        inits.isInitialized("user")
            ? new liaison.groble.domain.user.entity.QUser(forProperty("user"), inits.get("user"))
            : null;
  }
}
