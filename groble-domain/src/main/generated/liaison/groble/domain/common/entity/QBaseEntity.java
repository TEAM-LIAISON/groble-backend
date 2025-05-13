package liaison.groble.domain.common.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import javax.annotation.processing.Generated;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.*;

/** QBaseEntity is a Querydsl query type for BaseEntity */
@Generated("com.querydsl.codegen.DefaultSupertypeSerializer")
public class QBaseEntity extends EntityPathBase<BaseEntity> {

  private static final long serialVersionUID = 2109914576L;

  public static final QBaseEntity baseEntity = new QBaseEntity("baseEntity");

  public final DateTimePath<java.time.LocalDateTime> createdAt =
      createDateTime("createdAt", java.time.LocalDateTime.class);

  public final StringPath createdBy = createString("createdBy");

  public final BooleanPath deleted = createBoolean("deleted");

  public final DateTimePath<java.time.LocalDateTime> updatedAt =
      createDateTime("updatedAt", java.time.LocalDateTime.class);

  public final StringPath updatedBy = createString("updatedBy");

  public QBaseEntity(String variable) {
    super(BaseEntity.class, forVariable(variable));
  }

  public QBaseEntity(Path<? extends BaseEntity> path) {
    super(path.getType(), path.getMetadata());
  }

  public QBaseEntity(PathMetadata metadata) {
    super(BaseEntity.class, metadata);
  }
}
