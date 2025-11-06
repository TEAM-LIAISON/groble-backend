package liaison.groble.domain.dashboard.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import javax.annotation.processing.Generated;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.*;

/** QContentReferrerEvent is a Querydsl query type for ContentReferrerEvent */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QContentReferrerEvent extends EntityPathBase<ContentReferrerEvent> {

  private static final long serialVersionUID = -984084161L;

  public static final QContentReferrerEvent contentReferrerEvent =
      new QContentReferrerEvent("contentReferrerEvent");

  public final NumberPath<Long> contentId = createNumber("contentId", Long.class);

  public final DateTimePath<java.time.LocalDateTime> eventDate =
      createDateTime("eventDate", java.time.LocalDateTime.class);

  public final NumberPath<Long> id = createNumber("id", Long.class);

  public final NumberPath<Long> referrerStatsId = createNumber("referrerStatsId", Long.class);

  public QContentReferrerEvent(String variable) {
    super(ContentReferrerEvent.class, forVariable(variable));
  }

  public QContentReferrerEvent(Path<? extends ContentReferrerEvent> path) {
    super(path.getType(), path.getMetadata());
  }

  public QContentReferrerEvent(PathMetadata metadata) {
    super(ContentReferrerEvent.class, metadata);
  }
}
