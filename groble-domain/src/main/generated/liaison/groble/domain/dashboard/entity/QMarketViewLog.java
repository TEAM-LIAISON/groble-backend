package liaison.groble.domain.dashboard.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import javax.annotation.processing.Generated;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.*;

/** QMarketViewLog is a Querydsl query type for MarketViewLog */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMarketViewLog extends EntityPathBase<MarketViewLog> {

  private static final long serialVersionUID = -566692442L;

  public static final QMarketViewLog marketViewLog = new QMarketViewLog("marketViewLog");

  public final NumberPath<Long> id = createNumber("id", Long.class);

  public final NumberPath<Long> marketId = createNumber("marketId", Long.class);

  public final StringPath userAgent = createString("userAgent");

  public final DateTimePath<java.time.LocalDateTime> viewedAt =
      createDateTime("viewedAt", java.time.LocalDateTime.class);

  public final NumberPath<Long> viewerId = createNumber("viewerId", Long.class);

  public final StringPath viewerIp = createString("viewerIp");

  public final StringPath visitorHash = createString("visitorHash");

  public QMarketViewLog(String variable) {
    super(MarketViewLog.class, forVariable(variable));
  }

  public QMarketViewLog(Path<? extends MarketViewLog> path) {
    super(path.getType(), path.getMetadata());
  }

  public QMarketViewLog(PathMetadata metadata) {
    super(MarketViewLog.class, metadata);
  }
}
