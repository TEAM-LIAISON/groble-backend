package liaison.groble.domain.settlement.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import javax.annotation.processing.Generated;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.*;

/** QFeePolicy is a Querydsl query type for FeePolicy */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QFeePolicy extends EntityPathBase<FeePolicy> {

  private static final long serialVersionUID = 398167966L;

  public static final QFeePolicy feePolicy = new QFeePolicy("feePolicy");

  public final liaison.groble.domain.common.entity.QBaseTimeEntity _super =
      new liaison.groble.domain.common.entity.QBaseTimeEntity(this);

  // inherited
  public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

  public final DateTimePath<java.time.LocalDateTime> effectiveFrom =
      createDateTime("effectiveFrom", java.time.LocalDateTime.class);

  public final DateTimePath<java.time.LocalDateTime> effectiveTo =
      createDateTime("effectiveTo", java.time.LocalDateTime.class);

  public final NumberPath<Long> id = createNumber("id", Long.class);

  public final NumberPath<java.math.BigDecimal> pgFeeRateApplied =
      createNumber("pgFeeRateApplied", java.math.BigDecimal.class);

  public final NumberPath<java.math.BigDecimal> pgFeeRateBaseline =
      createNumber("pgFeeRateBaseline", java.math.BigDecimal.class);

  public final NumberPath<java.math.BigDecimal> pgFeeRateDisplay =
      createNumber("pgFeeRateDisplay", java.math.BigDecimal.class);

  public final NumberPath<java.math.BigDecimal> platformFeeRateApplied =
      createNumber("platformFeeRateApplied", java.math.BigDecimal.class);

  public final NumberPath<java.math.BigDecimal> platformFeeRateBaseline =
      createNumber("platformFeeRateBaseline", java.math.BigDecimal.class);

  public final NumberPath<java.math.BigDecimal> platformFeeRateDisplay =
      createNumber("platformFeeRateDisplay", java.math.BigDecimal.class);

  public final NumberPath<Long> scopeReference = createNumber("scopeReference", Long.class);

  public final EnumPath<liaison.groble.domain.settlement.enums.FeePolicyScope> scopeType =
      createEnum("scopeType", liaison.groble.domain.settlement.enums.FeePolicyScope.class);

  // inherited
  public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

  public final NumberPath<java.math.BigDecimal> vatRate =
      createNumber("vatRate", java.math.BigDecimal.class);

  public QFeePolicy(String variable) {
    super(FeePolicy.class, forVariable(variable));
  }

  public QFeePolicy(Path<? extends FeePolicy> path) {
    super(path.getType(), path.getMetadata());
  }

  public QFeePolicy(PathMetadata metadata) {
    super(FeePolicy.class, metadata);
  }
}
