package liaison.groble.domain.guest.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import javax.annotation.processing.Generated;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.*;

/** QGuestUser is a Querydsl query type for GuestUser */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QGuestUser extends EntityPathBase<GuestUser> {

  private static final long serialVersionUID = -153293406L;

  public static final QGuestUser guestUser = new QGuestUser("guestUser");

  public final liaison.groble.domain.common.entity.QBaseTimeEntity _super =
      new liaison.groble.domain.common.entity.QBaseTimeEntity(this);

  // inherited
  public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

  public final StringPath email = createString("email");

  public final NumberPath<Long> id = createNumber("id", Long.class);

  public final StringPath phoneNumber = createString("phoneNumber");

  public final EnumPath<liaison.groble.domain.guest.enums.PhoneVerificationStatus>
      phoneVerificationStatus =
          createEnum(
              "phoneVerificationStatus",
              liaison.groble.domain.guest.enums.PhoneVerificationStatus.class);

  public final DateTimePath<java.time.LocalDateTime> phoneVerifiedAt =
      createDateTime("phoneVerifiedAt", java.time.LocalDateTime.class);

  // inherited
  public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

  public final StringPath username = createString("username");

  public final DateTimePath<java.time.LocalDateTime> verificationExpiresAt =
      createDateTime("verificationExpiresAt", java.time.LocalDateTime.class);

  public QGuestUser(String variable) {
    super(GuestUser.class, forVariable(variable));
  }

  public QGuestUser(Path<? extends GuestUser> path) {
    super(path.getType(), path.getMetadata());
  }

  public QGuestUser(PathMetadata metadata) {
    super(GuestUser.class, metadata);
  }
}
