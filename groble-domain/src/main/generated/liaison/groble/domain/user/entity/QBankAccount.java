package liaison.groble.domain.user.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QBankAccount is a Querydsl query type for BankAccount
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QBankAccount extends EntityPathBase<BankAccount> {

    private static final long serialVersionUID = -1106135755L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QBankAccount bankAccount = new QBankAccount("bankAccount");

    public final liaison.groble.domain.common.entity.QBaseTimeEntity _super = new liaison.groble.domain.common.entity.QBaseTimeEntity(this);

    public final StringPath accountHolderId = createString("accountHolderId");

    public final StringPath accountHolderName = createString("accountHolderName");

    public final StringPath accountHolderType = createString("accountHolderType");

    public final StringPath accountNumber = createString("accountNumber");

    public final StringPath bankCode = createString("bankCode");

    public final StringPath bankName = createString("bankName");

    //inherited
    public final DateTimePath<java.time.Instant> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isPrimary = createBoolean("isPrimary");

    //inherited
    public final DateTimePath<java.time.Instant> modifiedAt = _super.modifiedAt;

    public final EnumPath<liaison.groble.domain.user.enums.BankAccountStatus> status = createEnum("status", liaison.groble.domain.user.enums.BankAccountStatus.class);

    public final QUser user;

    public final ListPath<BankAccountVerification, QBankAccountVerification> verifications = this.<BankAccountVerification, QBankAccountVerification>createList("verifications", BankAccountVerification.class, QBankAccountVerification.class, PathInits.DIRECT2);

    public final DateTimePath<java.time.LocalDateTime> verifiedAt = createDateTime("verifiedAt", java.time.LocalDateTime.class);

    public QBankAccount(String variable) {
        this(BankAccount.class, forVariable(variable), INITS);
    }

    public QBankAccount(Path<? extends BankAccount> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QBankAccount(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QBankAccount(PathMetadata metadata, PathInits inits) {
        this(BankAccount.class, metadata, inits);
    }

    public QBankAccount(Class<? extends BankAccount> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new QUser(forProperty("user"), inits.get("user")) : null;
    }

}

