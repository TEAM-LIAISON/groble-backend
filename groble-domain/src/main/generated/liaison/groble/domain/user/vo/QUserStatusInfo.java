package liaison.groble.domain.user.vo;

import static com.querydsl.core.types.PathMetadataFactory.*;

import javax.annotation.processing.Generated;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.*;

/** QUserStatusInfo is a Querydsl query type for UserStatusInfo */
@Generated("com.querydsl.codegen.DefaultEmbeddableSerializer")
public class QUserStatusInfo extends BeanPath<UserStatusInfo> {

  private static final long serialVersionUID = -2088389263L;

  public static final QUserStatusInfo userStatusInfo = new QUserStatusInfo("userStatusInfo");

  public final EnumPath<liaison.groble.domain.user.enums.UserStatus> status =
      createEnum("status", liaison.groble.domain.user.enums.UserStatus.class);

  public final DateTimePath<java.time.Instant> statusChangedAt =
      createDateTime("statusChangedAt", java.time.Instant.class);

  public QUserStatusInfo(String variable) {
    super(UserStatusInfo.class, forVariable(variable));
  }

  public QUserStatusInfo(Path<? extends UserStatusInfo> path) {
    super(path.getType(), path.getMetadata());
  }

  public QUserStatusInfo(PathMetadata metadata) {
    super(UserStatusInfo.class, metadata);
  }
}
