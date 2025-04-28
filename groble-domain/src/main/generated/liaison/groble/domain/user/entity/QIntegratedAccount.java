package liaison.groble.domain.user.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import javax.annotation.processing.Generated;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.*;
import com.querydsl.core.types.dsl.PathInits;

/** QIntegratedAccount is a Querydsl query type for IntegratedAccount */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QIntegratedAccount extends EntityPathBase<IntegratedAccount> {

  private static final long serialVersionUID = -1442356418L;

  private static final PathInits INITS = PathInits.DIRECT2;

  public static final QIntegratedAccount integratedAccount =
      new QIntegratedAccount("integratedAccount");

  public final NumberPath<Long> id = createNumber("id", Long.class);

  public final StringPath integratedAccountEmail = createString("integratedAccountEmail");

  public final StringPath password = createString("password");

  public final QUser user;

  public QIntegratedAccount(String variable) {
    this(IntegratedAccount.class, forVariable(variable), INITS);
  }

  public QIntegratedAccount(Path<? extends IntegratedAccount> path) {
    this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
  }

  public QIntegratedAccount(PathMetadata metadata) {
    this(metadata, PathInits.getFor(metadata, INITS));
  }

  public QIntegratedAccount(PathMetadata metadata, PathInits inits) {
    this(IntegratedAccount.class, metadata, inits);
  }

  public QIntegratedAccount(
      Class<? extends IntegratedAccount> type, PathMetadata metadata, PathInits inits) {
    super(type, metadata, inits);
    this.user =
        inits.isInitialized("user") ? new QUser(forProperty("user"), inits.get("user")) : null;
  }
}
