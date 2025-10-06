package liaison.groble.domain.dashboard.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QReferrerTracking is a Querydsl query type for ReferrerTracking
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QReferrerTracking extends EntityPathBase<ReferrerTracking> {

    private static final long serialVersionUID = -571458061L;

    public static final QReferrerTracking referrerTracking = new QReferrerTracking("referrerTracking");

    public final liaison.groble.domain.common.entity.QBaseTimeEntity _super = new liaison.groble.domain.common.entity.QBaseTimeEntity(this);

    public final StringPath contentId = createString("contentId");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final DateTimePath<java.time.LocalDateTime> eventTimestamp = createDateTime("eventTimestamp", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath ipAddress = createString("ipAddress");

    public final StringPath landingPageUrl = createString("landingPageUrl");

    public final StringPath lastPageUrl = createString("lastPageUrl");

    public final StringPath marketLinkUrl = createString("marketLinkUrl");

    public final StringPath pageUrl = createString("pageUrl");

    public final StringPath referrerChain = createString("referrerChain");

    public final StringPath referrerMetadata = createString("referrerMetadata");

    public final StringPath referrerUrl = createString("referrerUrl");

    public final StringPath sessionId = createString("sessionId");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final StringPath userAgent = createString("userAgent");

    public final StringPath utmCampaign = createString("utmCampaign");

    public final StringPath utmContent = createString("utmContent");

    public final StringPath utmMedium = createString("utmMedium");

    public final StringPath utmSource = createString("utmSource");

    public final StringPath utmTerm = createString("utmTerm");

    public QReferrerTracking(String variable) {
        super(ReferrerTracking.class, forVariable(variable));
    }

    public QReferrerTracking(Path<? extends ReferrerTracking> path) {
        super(path.getType(), path.getMetadata());
    }

    public QReferrerTracking(PathMetadata metadata) {
        super(ReferrerTracking.class, metadata);
    }

}

