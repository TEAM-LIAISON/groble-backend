package liaison.groble.domain.dashboard.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QMarketReferrerStats is a Querydsl query type for MarketReferrerStats
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMarketReferrerStats extends EntityPathBase<MarketReferrerStats> {

    private static final long serialVersionUID = 510625223L;

    public static final QMarketReferrerStats marketReferrerStats = new QMarketReferrerStats("marketReferrerStats");

    public final StringPath campaign = createString("campaign");

    public final StringPath content = createString("content");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> marketId = createNumber("marketId", Long.class);

    public final StringPath medium = createString("medium");

    public final StringPath referrerDomain = createString("referrerDomain");

    public final StringPath referrerPath = createString("referrerPath");

    public final StringPath referrerUrl = createString("referrerUrl");

    public final StringPath source = createString("source");

    public final StringPath term = createString("term");

    public QMarketReferrerStats(String variable) {
        super(MarketReferrerStats.class, forVariable(variable));
    }

    public QMarketReferrerStats(Path<? extends MarketReferrerStats> path) {
        super(path.getType(), path.getMetadata());
    }

    public QMarketReferrerStats(PathMetadata metadata) {
        super(MarketReferrerStats.class, metadata);
    }

}

