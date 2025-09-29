package liaison.groble.domain.hometest.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QHomeTestContact is a Querydsl query type for HomeTestContact
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QHomeTestContact extends EntityPathBase<HomeTestContact> {

    private static final long serialVersionUID = 1160027341L;

    public static final QHomeTestContact homeTestContact = new QHomeTestContact("homeTestContact");

    public final liaison.groble.domain.common.entity.QBaseTimeEntity _super = new liaison.groble.domain.common.entity.QBaseTimeEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath email = createString("email");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath nickname = createString("nickname");

    public final StringPath phoneNumber = createString("phoneNumber");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QHomeTestContact(String variable) {
        super(HomeTestContact.class, forVariable(variable));
    }

    public QHomeTestContact(Path<? extends HomeTestContact> path) {
        super(path.getType(), path.getMetadata());
    }

    public QHomeTestContact(PathMetadata metadata) {
        super(HomeTestContact.class, metadata);
    }

}

