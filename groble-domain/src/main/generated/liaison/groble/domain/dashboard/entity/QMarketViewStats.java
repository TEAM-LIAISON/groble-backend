package liaison.groble.domain.dashboard.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QMarketViewStats is a Querydsl query type for MarketViewStats
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMarketViewStats extends EntityPathBase<MarketViewStats> {

    private static final long serialVersionUID = 876021377L;

    public static final QMarketViewStats marketViewStats = new QMarketViewStats("marketViewStats");

    public final liaison.groble.domain.common.entity.QBaseTimeEntity _super = new liaison.groble.domain.common.entity.QBaseTimeEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> loggedInViewerCount = createNumber("loggedInViewerCount", Long.class);

    public final NumberPath<Long> marketId = createNumber("marketId", Long.class);

    public final EnumPath<liaison.groble.domain.common.enums.PeriodType> periodType = createEnum("periodType", liaison.groble.domain.common.enums.PeriodType.class);

    public final DatePath<java.time.LocalDate> statDate = createDate("statDate", java.time.LocalDate.class);

    public final NumberPath<Long> uniqueViewerCount = createNumber("uniqueViewerCount", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final NumberPath<Long> viewCount = createNumber("viewCount", Long.class);

    public QMarketViewStats(String variable) {
        super(MarketViewStats.class, forVariable(variable));
    }

    public QMarketViewStats(Path<? extends MarketViewStats> path) {
        super(path.getType(), path.getMetadata());
    }

    public QMarketViewStats(PathMetadata metadata) {
        super(MarketViewStats.class, metadata);
    }

}

