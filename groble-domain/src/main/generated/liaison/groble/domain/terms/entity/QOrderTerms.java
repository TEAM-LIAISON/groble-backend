package liaison.groble.domain.terms.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import javax.annotation.processing.Generated;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.*;
import com.querydsl.core.types.dsl.PathInits;

/** QOrderTerms is a Querydsl query type for OrderTerms */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QOrderTerms extends EntityPathBase<OrderTerms> {

  private static final long serialVersionUID = 1052823753L;

  public static final QOrderTerms orderTerms = new QOrderTerms("orderTerms");

  public final ListPath<UserOrderTerms, QUserOrderTerms> agreements =
      this.<UserOrderTerms, QUserOrderTerms>createList(
          "agreements", UserOrderTerms.class, QUserOrderTerms.class, PathInits.DIRECT2);

  public final StringPath contentUrl = createString("contentUrl");

  public final DateTimePath<java.time.LocalDateTime> effectiveFrom =
      createDateTime("effectiveFrom", java.time.LocalDateTime.class);

  public final DateTimePath<java.time.LocalDateTime> effectiveTo =
      createDateTime("effectiveTo", java.time.LocalDateTime.class);

  public final NumberPath<Long> id = createNumber("id", Long.class);

  public final StringPath title = createString("title");

  public final EnumPath<liaison.groble.domain.terms.enums.OrderTermsType> type =
      createEnum("type", liaison.groble.domain.terms.enums.OrderTermsType.class);

  public final StringPath version = createString("version");

  public QOrderTerms(String variable) {
    super(OrderTerms.class, forVariable(variable));
  }

  public QOrderTerms(Path<? extends OrderTerms> path) {
    super(path.getType(), path.getMetadata());
  }

  public QOrderTerms(PathMetadata metadata) {
    super(OrderTerms.class, metadata);
  }
}
