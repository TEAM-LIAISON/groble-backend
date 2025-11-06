package liaison.groble.domain.role.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import javax.annotation.processing.Generated;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.*;
import com.querydsl.core.types.dsl.PathInits;

/** QUserRole is a Querydsl query type for UserRole */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserRole extends EntityPathBase<UserRole> {

  private static final long serialVersionUID = -894314712L;

  private static final PathInits INITS = PathInits.DIRECT2;

  public static final QUserRole userRole = new QUserRole("userRole");

  public final DateTimePath<java.time.LocalDateTime> assignedAt =
      createDateTime("assignedAt", java.time.LocalDateTime.class);

  public final NumberPath<Long> id = createNumber("id", Long.class);

  public final QRole role;

  public final liaison.groble.domain.user.entity.QUser user;

  public QUserRole(String variable) {
    this(UserRole.class, forVariable(variable), INITS);
  }

  public QUserRole(Path<? extends UserRole> path) {
    this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
  }

  public QUserRole(PathMetadata metadata) {
    this(metadata, PathInits.getFor(metadata, INITS));
  }

  public QUserRole(PathMetadata metadata, PathInits inits) {
    this(UserRole.class, metadata, inits);
  }

  public QUserRole(Class<? extends UserRole> type, PathMetadata metadata, PathInits inits) {
    super(type, metadata, inits);
    this.role = inits.isInitialized("role") ? new QRole(forProperty("role")) : null;
    this.user =
        inits.isInitialized("user")
            ? new liaison.groble.domain.user.entity.QUser(forProperty("user"), inits.get("user"))
            : null;
  }
}
