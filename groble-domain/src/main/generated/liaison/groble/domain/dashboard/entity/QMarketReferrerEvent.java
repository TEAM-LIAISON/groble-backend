package liaison.groble.domain.dashboard.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QMarketReferrerEvent is a Querydsl query type for MarketReferrerEvent
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMarketReferrerEvent extends EntityPathBase<MarketReferrerEvent> {

    private static final long serialVersionUID = 497759170L;

    public static final QMarketReferrerEvent marketReferrerEvent = new QMarketReferrerEvent("marketReferrerEvent");

    public final DateTimePath<java.time.LocalDateTime> eventDate = createDateTime("eventDate", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> marketId = createNumber("marketId", Long.class);

    public final NumberPath<Long> referrerStatsId = createNumber("referrerStatsId", Long.class);

    public QMarketReferrerEvent(String variable) {
        super(MarketReferrerEvent.class, forVariable(variable));
    }

    public QMarketReferrerEvent(Path<? extends MarketReferrerEvent> path) {
        super(path.getType(), path.getMetadata());
    }

    public QMarketReferrerEvent(PathMetadata metadata) {
        super(MarketReferrerEvent.class, metadata);
    }

}

