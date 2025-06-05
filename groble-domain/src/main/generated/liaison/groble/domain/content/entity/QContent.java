package liaison.groble.domain.content.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QContent is a Querydsl query type for Content
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QContent extends EntityPathBase<Content> {

    private static final long serialVersionUID = -323075497L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QContent content = new QContent("content");

    public final liaison.groble.domain.common.entity.QBaseTimeEntity _super = new liaison.groble.domain.common.entity.QBaseTimeEntity(this);

    public final QCategory category;

    public final ListPath<String, StringPath> contentDetailImageUrls = this.<String, StringPath>createList("contentDetailImageUrls", String.class, StringPath.class, PathInits.DIRECT2);

    public final StringPath contentIntroduction = createString("contentIntroduction");

    public final EnumPath<liaison.groble.domain.content.enums.ContentType> contentType = createEnum("contentType", liaison.groble.domain.content.enums.ContentType.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<java.math.BigDecimal> lowestPrice = createNumber("lowestPrice", java.math.BigDecimal.class);

    public final StringPath makerIntro = createString("makerIntro");

    public final ListPath<ContentOption, QContentOption> options = this.<ContentOption, QContentOption>createList("options", ContentOption.class, QContentOption.class, PathInits.DIRECT2);

    public final StringPath rejectReason = createString("rejectReason");

    public final NumberPath<Integer> saleCount = createNumber("saleCount", Integer.class);

    public final StringPath serviceProcess = createString("serviceProcess");

    public final StringPath serviceTarget = createString("serviceTarget");

    public final NumberPath<Integer> sortOrder = createNumber("sortOrder", Integer.class);

    public final EnumPath<liaison.groble.domain.content.enums.ContentStatus> status = createEnum("status", liaison.groble.domain.content.enums.ContentStatus.class);

    public final StringPath thumbnailUrl = createString("thumbnailUrl");

    public final StringPath title = createString("title");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final liaison.groble.domain.user.entity.QUser user;

    public final NumberPath<Long> viewCount = createNumber("viewCount", Long.class);

    public QContent(String variable) {
        this(Content.class, forVariable(variable), INITS);
    }

    public QContent(Path<? extends Content> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QContent(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QContent(PathMetadata metadata, PathInits inits) {
        this(Content.class, metadata, inits);
    }

    public QContent(Class<? extends Content> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.category = inits.isInitialized("category") ? new QCategory(forProperty("category"), inits.get("category")) : null;
        this.user = inits.isInitialized("user") ? new liaison.groble.domain.user.entity.QUser(forProperty("user"), inits.get("user")) : null;
    }

}

