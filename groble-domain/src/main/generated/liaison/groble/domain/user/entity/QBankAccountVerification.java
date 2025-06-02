package liaison.groble.domain.user.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QBankAccountVerification is a Querydsl query type for BankAccountVerification
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QBankAccountVerification extends EntityPathBase<BankAccountVerification> {

    private static final long serialVersionUID = -493111312L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QBankAccountVerification bankAccountVerification = new QBankAccountVerification("bankAccountVerification");

    public final liaison.groble.domain.common.entity.QBaseTimeEntity _super = new liaison.groble.domain.common.entity.QBaseTimeEntity(this);

    public final QBankAccount bankAccount;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath depositAccountNumber = createString("depositAccountNumber");

    public final StringPath depositBank = createString("depositBank");

    public final NumberPath<java.math.BigDecimal> depositPrice = createNumber("depositPrice", java.math.BigDecimal.class);

    public final DateTimePath<java.time.LocalDateTime> expiredAt = createDateTime("expiredAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final MapPath<String, Object, SimplePath<Object>> requestData = this.<String, Object, SimplePath<Object>>createMap("requestData", String.class, Object.class, SimplePath.class);

    public final MapPath<String, Object, SimplePath<Object>> responseData = this.<String, Object, SimplePath<Object>>createMap("responseData", String.class, Object.class, SimplePath.class);

    public final EnumPath<liaison.groble.domain.user.enums.BankAccountVerificationStatus> status = createEnum("status", liaison.groble.domain.user.enums.BankAccountVerificationStatus.class);

    public final EnumPath<liaison.groble.domain.user.enums.BankAccountVerificationType> type = createEnum("type", liaison.groble.domain.user.enums.BankAccountVerificationType.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final StringPath verificationKey = createString("verificationKey");

    public final DateTimePath<java.time.LocalDateTime> verifiedAt = createDateTime("verifiedAt", java.time.LocalDateTime.class);

    public QBankAccountVerification(String variable) {
        this(BankAccountVerification.class, forVariable(variable), INITS);
    }

    public QBankAccountVerification(Path<? extends BankAccountVerification> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QBankAccountVerification(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QBankAccountVerification(PathMetadata metadata, PathInits inits) {
        this(BankAccountVerification.class, metadata, inits);
    }

    public QBankAccountVerification(Class<? extends BankAccountVerification> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.bankAccount = inits.isInitialized("bankAccount") ? new QBankAccount(forProperty("bankAccount"), inits.get("bankAccount")) : null;
    }

}

