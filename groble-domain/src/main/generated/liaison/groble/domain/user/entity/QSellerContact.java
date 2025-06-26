package liaison.groble.domain.user.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSellerContact is a Querydsl query type for SellerContact
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSellerContact extends EntityPathBase<SellerContact> {

    private static final long serialVersionUID = -1826817147L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QSellerContact sellerContact = new QSellerContact("sellerContact");

    public final EnumPath<liaison.groble.domain.user.enums.ContactType> contactType = createEnum("contactType", liaison.groble.domain.user.enums.ContactType.class);

    public final StringPath contactValue = createString("contactValue");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QUser user;

    public QSellerContact(String variable) {
        this(SellerContact.class, forVariable(variable), INITS);
    }

    public QSellerContact(Path<? extends SellerContact> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QSellerContact(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QSellerContact(PathMetadata metadata, PathInits inits) {
        this(SellerContact.class, metadata, inits);
    }

    public QSellerContact(Class<? extends SellerContact> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new QUser(forProperty("user"), inits.get("user")) : null;
    }

}

