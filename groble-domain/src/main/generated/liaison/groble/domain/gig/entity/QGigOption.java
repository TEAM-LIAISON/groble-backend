package liaison.groble.domain.gig.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import javax.annotation.processing.Generated;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.*;
import com.querydsl.core.types.dsl.PathInits;

/** QGigOption is a Querydsl query type for GigOption */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QGigOption extends EntityPathBase<GigOption> {

  private static final long serialVersionUID = 662678316L;

  private static final PathInits INITS = PathInits.DIRECT2;

  public static final QGigOption gigOption = new QGigOption("gigOption");

  public final liaison.groble.domain.common.entity.QBaseTimeEntity _super =
      new liaison.groble.domain.common.entity.QBaseTimeEntity(this);

  // inherited
  public final DateTimePath<java.time.Instant> createdAt = _super.createdAt;

  public final StringPath description = createString("description");

  public final QGig gig;

  public final NumberPath<Long> id = createNumber("id", Long.class);

  // inherited
  public final DateTimePath<java.time.Instant> modifiedAt = _super.modifiedAt;

  public final StringPath name = createString("name");

  public final NumberPath<java.math.BigDecimal> price =
      createNumber("price", java.math.BigDecimal.class);

  public QGigOption(String variable) {
    this(GigOption.class, forVariable(variable), INITS);
  }

  public QGigOption(Path<? extends GigOption> path) {
    this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
  }

  public QGigOption(PathMetadata metadata) {
    this(metadata, PathInits.getFor(metadata, INITS));
  }

  public QGigOption(PathMetadata metadata, PathInits inits) {
    this(GigOption.class, metadata, inits);
  }

  public QGigOption(Class<? extends GigOption> type, PathMetadata metadata, PathInits inits) {
    super(type, metadata, inits);
    this.gig = inits.isInitialized("gig") ? new QGig(forProperty("gig"), inits.get("gig")) : null;
  }
}
