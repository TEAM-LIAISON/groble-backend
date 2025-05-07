package liaison.groble.domain.purchase.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import javax.annotation.processing.Generated;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.*;
import com.querydsl.core.types.dsl.PathInits;

/** QPurchase is a Querydsl query type for Purchase */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPurchase extends EntityPathBase<Purchase> {

  private static final long serialVersionUID = -1287926317L;

  private static final PathInits INITS = PathInits.DIRECT2;

  public static final QPurchase purchase = new QPurchase("purchase");

  public final liaison.groble.domain.content.entity.QContent content;

  public final NumberPath<Long> id = createNumber("id", Long.class);

  public final liaison.groble.domain.user.entity.QUser user;

  public QPurchase(String variable) {
    this(Purchase.class, forVariable(variable), INITS);
  }

  public QPurchase(Path<? extends Purchase> path) {
    this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
  }

  public QPurchase(PathMetadata metadata) {
    this(metadata, PathInits.getFor(metadata, INITS));
  }

  public QPurchase(PathMetadata metadata, PathInits inits) {
    this(Purchase.class, metadata, inits);
  }

  public QPurchase(Class<? extends Purchase> type, PathMetadata metadata, PathInits inits) {
    super(type, metadata, inits);
    this.content =
        inits.isInitialized("content")
            ? new liaison.groble.domain.content.entity.QContent(
                forProperty("content"), inits.get("content"))
            : null;
    this.user =
        inits.isInitialized("user")
            ? new liaison.groble.domain.user.entity.QUser(forProperty("user"), inits.get("user"))
            : null;
  }
}
