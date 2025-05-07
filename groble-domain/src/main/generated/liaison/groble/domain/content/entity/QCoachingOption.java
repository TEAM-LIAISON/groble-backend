package liaison.groble.domain.content.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import javax.annotation.processing.Generated;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.*;
import com.querydsl.core.types.dsl.PathInits;

/** QCoachingOption is a Querydsl query type for CoachingOption */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCoachingOption extends EntityPathBase<CoachingOption> {

  private static final long serialVersionUID = 1400168447L;

  private static final PathInits INITS = PathInits.DIRECT2;

  public static final QCoachingOption coachingOption = new QCoachingOption("coachingOption");

  public final QContentOption _super;

  public final EnumPath<liaison.groble.domain.content.enums.CoachingPeriod> coachingPeriod =
      createEnum("coachingPeriod", liaison.groble.domain.content.enums.CoachingPeriod.class);

  public final EnumPath<liaison.groble.domain.content.enums.CoachingType> coachingType =
      createEnum("coachingType", liaison.groble.domain.content.enums.CoachingType.class);

  public final StringPath coachingTypeDescription = createString("coachingTypeDescription");

  // inherited
  public final QContent content;

  // inherited
  public final DateTimePath<java.time.Instant> createdAt;

  // inherited
  public final StringPath description;

  public final EnumPath<liaison.groble.domain.content.enums.DocumentProvision> documentProvision =
      createEnum("documentProvision", liaison.groble.domain.content.enums.DocumentProvision.class);

  // inherited
  public final NumberPath<Long> id;

  // inherited
  public final DateTimePath<java.time.Instant> modifiedAt;

  // inherited
  public final StringPath name;

  // inherited
  public final NumberPath<java.math.BigDecimal> price;

  public QCoachingOption(String variable) {
    this(CoachingOption.class, forVariable(variable), INITS);
  }

  public QCoachingOption(Path<? extends CoachingOption> path) {
    this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
  }

  public QCoachingOption(PathMetadata metadata) {
    this(metadata, PathInits.getFor(metadata, INITS));
  }

  public QCoachingOption(PathMetadata metadata, PathInits inits) {
    this(CoachingOption.class, metadata, inits);
  }

  public QCoachingOption(
      Class<? extends CoachingOption> type, PathMetadata metadata, PathInits inits) {
    super(type, metadata, inits);
    this._super = new QContentOption(type, metadata, inits);
    this.content = _super.content;
    this.createdAt = _super.createdAt;
    this.description = _super.description;
    this.id = _super.id;
    this.modifiedAt = _super.modifiedAt;
    this.name = _super.name;
    this.price = _super.price;
  }
}
