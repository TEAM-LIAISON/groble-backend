package liaison.groble.domain.content.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QContentViewStats is a Querydsl query type for ContentViewStats
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QContentViewStats extends EntityPathBase<ContentViewStats> {

    private static final long serialVersionUID = -1775761309L;

    public static final QContentViewStats contentViewStats = new QContentViewStats("contentViewStats");

    public final liaison.groble.domain.common.entity.QBaseTimeEntity _super = new liaison.groble.domain.common.entity.QBaseTimeEntity(this);

    public final NumberPath<Long> contentId = createNumber("contentId", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> loggedInViewerCount = createNumber("loggedInViewerCount", Long.class);

    public final EnumPath<liaison.groble.domain.market.entity.MarketViewStats.PeriodType> periodType = createEnum("periodType", liaison.groble.domain.market.entity.MarketViewStats.PeriodType.class);

    public final DatePath<java.time.LocalDate> statDate = createDate("statDate", java.time.LocalDate.class);

    public final NumberPath<Long> uniqueViewerCount = createNumber("uniqueViewerCount", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final NumberPath<Long> viewCount = createNumber("viewCount", Long.class);

    public QContentViewStats(String variable) {
        super(ContentViewStats.class, forVariable(variable));
    }

    public QContentViewStats(Path<? extends ContentViewStats> path) {
        super(path.getType(), path.getMetadata());
    }

    public QContentViewStats(PathMetadata metadata) {
        super(ContentViewStats.class, metadata);
    }

}

