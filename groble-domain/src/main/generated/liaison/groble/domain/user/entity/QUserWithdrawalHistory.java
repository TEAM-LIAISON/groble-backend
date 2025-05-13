package liaison.groble.domain.user.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import javax.annotation.processing.Generated;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.*;

/** QUserWithdrawalHistory is a Querydsl query type for UserWithdrawalHistory */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserWithdrawalHistory extends EntityPathBase<UserWithdrawalHistory> {

  private static final long serialVersionUID = 1735411064L;

  public static final QUserWithdrawalHistory userWithdrawalHistory =
      new QUserWithdrawalHistory("userWithdrawalHistory");

  public final StringPath additionalComment = createString("additionalComment");

  public final StringPath email = createString("email");

  public final NumberPath<Long> id = createNumber("id", Long.class);

  public final EnumPath<liaison.groble.domain.user.enums.WithdrawalReason> reason =
      createEnum("reason", liaison.groble.domain.user.enums.WithdrawalReason.class);

  public final NumberPath<Long> userId = createNumber("userId", Long.class);

  public final DateTimePath<java.time.LocalDateTime> withdrawalDate =
      createDateTime("withdrawalDate", java.time.LocalDateTime.class);

  public QUserWithdrawalHistory(String variable) {
    super(UserWithdrawalHistory.class, forVariable(variable));
  }

  public QUserWithdrawalHistory(Path<? extends UserWithdrawalHistory> path) {
    super(path.getType(), path.getMetadata());
  }

  public QUserWithdrawalHistory(PathMetadata metadata) {
    super(UserWithdrawalHistory.class, metadata);
  }
}
