package liaison.groble.domain.user.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QVerificationToken is a Querydsl query type for VerificationToken
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QVerificationToken extends EntityPathBase<VerificationToken> {

    private static final long serialVersionUID = -946592126L;

    public static final QVerificationToken verificationToken = new QVerificationToken("verificationToken");

    public final StringPath email = createString("email");

    public final DateTimePath<java.time.LocalDateTime> expiryDate = createDateTime("expiryDate", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath token = createString("token");

    public final BooleanPath verified = createBoolean("verified");

    public QVerificationToken(String variable) {
        super(VerificationToken.class, forVariable(variable));
    }

    public QVerificationToken(Path<? extends VerificationToken> path) {
        super(path.getType(), path.getMetadata());
    }

    public QVerificationToken(PathMetadata metadata) {
        super(VerificationToken.class, metadata);
    }

}

