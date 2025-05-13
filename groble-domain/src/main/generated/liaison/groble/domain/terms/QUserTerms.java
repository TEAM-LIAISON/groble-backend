package liaison.groble.domain.terms;

import static com.querydsl.core.types.PathMetadataFactory.*;

import javax.annotation.processing.Generated;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.*;
import com.querydsl.core.types.dsl.PathInits;

/** QUserTerms is a Querydsl query type for UserTerms */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserTerms extends EntityPathBase<UserTerms> {

  private static final long serialVersionUID = -1456231359L;

  private static final PathInits INITS = PathInits.DIRECT2;

  public static final QUserTerms userTerms = new QUserTerms("userTerms");

  public final liaison.groble.domain.common.entity.QBaseTimeEntity _super =
      new liaison.groble.domain.common.entity.QBaseTimeEntity(this);

  public final BooleanPath agreed = createBoolean("agreed");

  public final DateTimePath<java.time.Instant> agreedAt =
      createDateTime("agreedAt", java.time.Instant.class);

  public final StringPath agreedIp = createString("agreedIp");

  public final StringPath agreedUserAgent = createString("agreedUserAgent");

  // inherited
  public final DateTimePath<java.time.Instant> createdAt = _super.createdAt;

  public final NumberPath<Long> id = createNumber("id", Long.class);

  // inherited
  public final DateTimePath<java.time.Instant> modifiedAt = _super.modifiedAt;

  public final QTerms terms;

  public final liaison.groble.domain.user.entity.QUser user;

  public QUserTerms(String variable) {
    this(UserTerms.class, forVariable(variable), INITS);
  }

  public QUserTerms(Path<? extends UserTerms> path) {
    this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
  }

  public QUserTerms(PathMetadata metadata) {
    this(metadata, PathInits.getFor(metadata, INITS));
  }

  public QUserTerms(PathMetadata metadata, PathInits inits) {
    this(UserTerms.class, metadata, inits);
  }

  public QUserTerms(Class<? extends UserTerms> type, PathMetadata metadata, PathInits inits) {
    super(type, metadata, inits);
    this.terms = inits.isInitialized("terms") ? new QTerms(forProperty("terms")) : null;
    this.user =
        inits.isInitialized("user")
            ? new liaison.groble.domain.user.entity.QUser(forProperty("user"), inits.get("user"))
            : null;
  }
}
