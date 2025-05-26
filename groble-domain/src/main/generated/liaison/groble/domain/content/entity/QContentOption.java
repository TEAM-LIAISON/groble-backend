package liaison.groble.domain.content.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QContentOption is a Querydsl query type for ContentOption
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QContentOption extends EntityPathBase<ContentOption> {

    private static final long serialVersionUID = -923641428L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QContentOption contentOption = new QContentOption("contentOption");

    public final liaison.groble.domain.common.entity.QBaseTimeEntity _super = new liaison.groble.domain.common.entity.QBaseTimeEntity(this);

    public final QContent content;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath description = createString("description");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public final NumberPath<java.math.BigDecimal> price = createNumber("price", java.math.BigDecimal.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QContentOption(String variable) {
        this(ContentOption.class, forVariable(variable), INITS);
    }

    public QContentOption(Path<? extends ContentOption> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QContentOption(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QContentOption(PathMetadata metadata, PathInits inits) {
        this(ContentOption.class, metadata, inits);
    }

    public QContentOption(Class<? extends ContentOption> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.content = inits.isInitialized("content") ? new QContent(forProperty("content"), inits.get("content")) : null;
    }

}

