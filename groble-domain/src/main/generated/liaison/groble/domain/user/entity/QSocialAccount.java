package liaison.groble.domain.user.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import javax.annotation.processing.Generated;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.*;
import com.querydsl.core.types.dsl.PathInits;

/** QSocialAccount is a Querydsl query type for SocialAccount */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSocialAccount extends EntityPathBase<SocialAccount> {

  private static final long serialVersionUID = -2014678556L;

  private static final PathInits INITS = PathInits.DIRECT2;

  public static final QSocialAccount socialAccount = new QSocialAccount("socialAccount");

  public final NumberPath<Long> id = createNumber("id", Long.class);

  public final StringPath providerId = createString("providerId");

  public final EnumPath<liaison.groble.domain.user.enums.ProviderType> providerType =
      createEnum("providerType", liaison.groble.domain.user.enums.ProviderType.class);

  public final StringPath socialAccountEmail = createString("socialAccountEmail");

  public final QUser user;

  public QSocialAccount(String variable) {
    this(SocialAccount.class, forVariable(variable), INITS);
  }

  public QSocialAccount(Path<? extends SocialAccount> path) {
    this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
  }

  public QSocialAccount(PathMetadata metadata) {
    this(metadata, PathInits.getFor(metadata, INITS));
  }

  public QSocialAccount(PathMetadata metadata, PathInits inits) {
    this(SocialAccount.class, metadata, inits);
  }

  public QSocialAccount(
      Class<? extends SocialAccount> type, PathMetadata metadata, PathInits inits) {
    super(type, metadata, inits);
    this.user =
        inits.isInitialized("user") ? new QUser(forProperty("user"), inits.get("user")) : null;
  }
}
