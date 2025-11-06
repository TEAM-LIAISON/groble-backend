package liaison.groble.domain.dashboard.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import javax.annotation.processing.Generated;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.*;

/** QContentReferrerStats is a Querydsl query type for ContentReferrerStats */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QContentReferrerStats extends EntityPathBase<ContentReferrerStats> {

  private static final long serialVersionUID = -971218108L;

  public static final QContentReferrerStats contentReferrerStats =
      new QContentReferrerStats("contentReferrerStats");

  public final liaison.groble.domain.common.entity.QBaseTimeEntity _super =
      new liaison.groble.domain.common.entity.QBaseTimeEntity(this);

  public final StringPath campaign = createString("campaign");

  public final StringPath content = createString("content");

  public final NumberPath<Long> contentId = createNumber("contentId", Long.class);

  // inherited
  public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

  public final NumberPath<Long> id = createNumber("id", Long.class);

  public final StringPath medium = createString("medium");

  public final StringPath referrerDomain = createString("referrerDomain");

  public final StringPath referrerPath = createString("referrerPath");

  public final StringPath referrerUrl = createString("referrerUrl");

  public final StringPath source = createString("source");

  public final StringPath term = createString("term");

  // inherited
  public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

  public final NumberPath<Integer> visitCount = createNumber("visitCount", Integer.class);

  public QContentReferrerStats(String variable) {
    super(ContentReferrerStats.class, forVariable(variable));
  }

  public QContentReferrerStats(Path<? extends ContentReferrerStats> path) {
    super(path.getType(), path.getMetadata());
  }

  public QContentReferrerStats(PathMetadata metadata) {
    super(ContentReferrerStats.class, metadata);
  }
}
