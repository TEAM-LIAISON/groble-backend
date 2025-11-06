package liaison.groble.domain.content.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import javax.annotation.processing.Generated;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.*;
import com.querydsl.core.types.dsl.PathInits;

/** QDocumentOption is a Querydsl query type for DocumentOption */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QDocumentOption extends EntityPathBase<DocumentOption> {

  private static final long serialVersionUID = -522649134L;

  private static final PathInits INITS = PathInits.DIRECT2;

  public static final QDocumentOption documentOption = new QDocumentOption("documentOption");

  public final QContentOption _super;

  // inherited
  public final QContent content;

  // inherited
  public final DateTimePath<java.time.LocalDateTime> createdAt;

  // inherited
  public final DateTimePath<java.time.LocalDateTime> deactivatedAt;

  // inherited
  public final StringPath description;

  public final StringPath documentFileUrl = createString("documentFileUrl");

  public final StringPath documentLinkUrl = createString("documentLinkUrl");

  public final StringPath documentOriginalFileName = createString("documentOriginalFileName");

  // inherited
  public final NumberPath<Long> id;

  // inherited
  public final BooleanPath isActive;

  // inherited
  public final StringPath name;

  // inherited
  public final NumberPath<java.math.BigDecimal> price;

  // inherited
  public final DateTimePath<java.time.LocalDateTime> updatedAt;

  public QDocumentOption(String variable) {
    this(DocumentOption.class, forVariable(variable), INITS);
  }

  public QDocumentOption(Path<? extends DocumentOption> path) {
    this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
  }

  public QDocumentOption(PathMetadata metadata) {
    this(metadata, PathInits.getFor(metadata, INITS));
  }

  public QDocumentOption(PathMetadata metadata, PathInits inits) {
    this(DocumentOption.class, metadata, inits);
  }

  public QDocumentOption(
      Class<? extends DocumentOption> type, PathMetadata metadata, PathInits inits) {
    super(type, metadata, inits);
    this._super = new QContentOption(type, metadata, inits);
    this.content = _super.content;
    this.createdAt = _super.createdAt;
    this.deactivatedAt = _super.deactivatedAt;
    this.description = _super.description;
    this.id = _super.id;
    this.isActive = _super.isActive;
    this.name = _super.name;
    this.price = _super.price;
    this.updatedAt = _super.updatedAt;
  }
}
