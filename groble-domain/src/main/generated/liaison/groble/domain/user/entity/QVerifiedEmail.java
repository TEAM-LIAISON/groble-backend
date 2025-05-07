package liaison.groble.domain.user.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import javax.annotation.processing.Generated;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.*;

/** QVerifiedEmail is a Querydsl query type for VerifiedEmail */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QVerifiedEmail extends EntityPathBase<VerifiedEmail> {

  private static final long serialVersionUID = -569809512L;

  public static final QVerifiedEmail verifiedEmail = new QVerifiedEmail("verifiedEmail");

  public final StringPath email = createString("email");

  public final NumberPath<Long> id = createNumber("id", Long.class);

  public final DateTimePath<java.time.LocalDateTime> verifiedAt =
      createDateTime("verifiedAt", java.time.LocalDateTime.class);

  public QVerifiedEmail(String variable) {
    super(VerifiedEmail.class, forVariable(variable));
  }

  public QVerifiedEmail(Path<? extends VerifiedEmail> path) {
    super(path.getType(), path.getMetadata());
  }

  public QVerifiedEmail(PathMetadata metadata) {
    super(VerifiedEmail.class, metadata);
  }
}
