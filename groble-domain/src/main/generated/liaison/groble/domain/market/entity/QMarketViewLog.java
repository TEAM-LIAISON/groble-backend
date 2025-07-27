package liaison.groble.domain.market.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QMarketViewLog is a Querydsl query type for MarketViewLog
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMarketViewLog extends EntityPathBase<MarketViewLog> {

    private static final long serialVersionUID = 321629078L;

    public static final QMarketViewLog marketViewLog = new QMarketViewLog("marketViewLog");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> marketId = createNumber("marketId", Long.class);

    public final StringPath referer = createString("referer");

    public final StringPath userAgent = createString("userAgent");

    public final DateTimePath<java.time.LocalDateTime> viewedAt = createDateTime("viewedAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> viewerId = createNumber("viewerId", Long.class);

    public final StringPath viewerIp = createString("viewerIp");

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

