package liaison.groble.domain.user.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import javax.annotation.processing.Generated;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.*;
import com.querydsl.core.types.dsl.PathInits;

/** QIdentityVerificationHistory is a Querydsl query type for IdentityVerificationHistory */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QIdentityVerificationHistory extends EntityPathBase<IdentityVerificationHistory> {

  private static final long serialVersionUID = -611737281L;

  private static final PathInits INITS = PathInits.DIRECT2;

  public static final QIdentityVerificationHistory identityVerificationHistory =
      new QIdentityVerificationHistory("identityVerificationHistory");

  public final liaison.groble.domain.common.entity.QBaseEntity _super =
      new liaison.groble.domain.common.entity.QBaseEntity(this);

  public final EnumPath<liaison.groble.domain.user.enums.IdentityVerificationStatus> afterStatus =
      createEnum("afterStatus", liaison.groble.domain.user.enums.IdentityVerificationStatus.class);

  public final EnumPath<liaison.groble.domain.user.enums.IdentityVerificationStatus> beforeStatus =
      createEnum("beforeStatus", liaison.groble.domain.user.enums.IdentityVerificationStatus.class);

  // inherited
  public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

  // inherited
  public final StringPath createdBy = _super.createdBy;

  // inherited
  public final BooleanPath deleted = _super.deleted;

  public final StringPath errorCode = createString("errorCode");

  public final StringPath errorMessage = createString("errorMessage");

  public final NumberPath<Long> id = createNumber("id", Long.class);

  public final StringPath portOneRequestId = createString("portOneRequestId");

  public final MapPath<String, Object, SimplePath<Object>> rawRequest =
      this.<String, Object, SimplePath<Object>>createMap(
          "rawRequest", String.class, Object.class, SimplePath.class);

  public final MapPath<String, Object, SimplePath<Object>> rawResponse =
      this.<String, Object, SimplePath<Object>>createMap(
          "rawResponse", String.class, Object.class, SimplePath.class);

  public final StringPath requestIp = createString("requestIp");

  public final BooleanPath success = createBoolean("success");

  public final StringPath transactionId = createString("transactionId");

  // inherited
  public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

  // inherited
  public final StringPath updatedBy = _super.updatedBy;

  public final QUser user;

  public final StringPath userAgent = createString("userAgent");

  public final EnumPath<liaison.groble.domain.user.vo.IdentityVerification.VerificationMethod>
      verificationMethod =
          createEnum(
              "verificationMethod",
              liaison.groble.domain.user.vo.IdentityVerification.VerificationMethod.class);

  public QIdentityVerificationHistory(String variable) {
    this(IdentityVerificationHistory.class, forVariable(variable), INITS);
  }

  public QIdentityVerificationHistory(Path<? extends IdentityVerificationHistory> path) {
    this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
  }

  public QIdentityVerificationHistory(PathMetadata metadata) {
    this(metadata, PathInits.getFor(metadata, INITS));
  }

  public QIdentityVerificationHistory(PathMetadata metadata, PathInits inits) {
    this(IdentityVerificationHistory.class, metadata, inits);
  }

  public QIdentityVerificationHistory(
      Class<? extends IdentityVerificationHistory> type, PathMetadata metadata, PathInits inits) {
    super(type, metadata, inits);
    this.user =
        inits.isInitialized("user") ? new QUser(forProperty("user"), inits.get("user")) : null;
  }
}
