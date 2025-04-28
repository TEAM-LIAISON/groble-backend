package liaison.groble.domain.terms;

import static com.querydsl.core.types.PathMetadataFactory.*;

import javax.annotation.processing.Generated;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.*;
import com.querydsl.core.types.dsl.PathInits;

/** QTerms is a Querydsl query type for Terms */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QTerms extends EntityPathBase<Terms> {

  private static final long serialVersionUID = 1557105612L;

  public static final QTerms terms = new QTerms("terms");

  public final ListPath<UserTerms, QUserTerms> agreements =
      this.<UserTerms, QUserTerms>createList(
          "agreements", UserTerms.class, QUserTerms.class, PathInits.DIRECT2);

  public final StringPath contentUrl = createString("contentUrl");

  public final DateTimePath<java.time.LocalDateTime> effectiveFrom =
      createDateTime("effectiveFrom", java.time.LocalDateTime.class);

  public final DateTimePath<java.time.LocalDateTime> effectiveTo =
      createDateTime("effectiveTo", java.time.LocalDateTime.class);

  public final NumberPath<Long> id = createNumber("id", Long.class);

  public final StringPath title = createString("title");

  public final EnumPath<liaison.groble.domain.terms.enums.TermsType> type =
      createEnum("type", liaison.groble.domain.terms.enums.TermsType.class);

  public final StringPath version = createString("version");

  public QTerms(String variable) {
    super(Terms.class, forVariable(variable));
  }

  public QTerms(Path<? extends Terms> path) {
    super(path.getType(), path.getMetadata());
  }

  public QTerms(PathMetadata metadata) {
    super(Terms.class, metadata);
  }
}
