package liaison.groble.domain.seller.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSellerInform is a Querydsl query type for SellerInform
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSellerInform extends EntityPathBase<SellerInform> {

    private static final long serialVersionUID = 1938684952L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QSellerInform sellerInform = new QSellerInform("sellerInform");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final liaison.groble.domain.user.entity.QUser user;

    public QSellerInform(String variable) {
        this(SellerInform.class, forVariable(variable), INITS);
    }

    public QSellerInform(Path<? extends SellerInform> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QSellerInform(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QSellerInform(PathMetadata metadata, PathInits inits) {
        this(SellerInform.class, metadata, inits);
    }

    public QSellerInform(Class<? extends SellerInform> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new liaison.groble.domain.user.entity.QUser(forProperty("user"), inits.get("user")) : null;
    }

}

