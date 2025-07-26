package liaison.groble.domain.user.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUser is a Querydsl query type for User
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUser extends EntityPathBase<User> {

    private static final long serialVersionUID = 405868391L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUser user = new QUser("user");

    public final liaison.groble.domain.common.entity.QBaseTimeEntity _super = new liaison.groble.domain.common.entity.QBaseTimeEntity(this);

    public final EnumPath<liaison.groble.domain.user.enums.AccountType> accountType = createEnum("accountType", liaison.groble.domain.user.enums.AccountType.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QIntegratedAccount integratedAccount;

    public final BooleanPath isSeller = createBoolean("isSeller");

    public final DateTimePath<java.time.Instant> lastLoginAt = createDateTime("lastLoginAt", java.time.Instant.class);

    public final EnumPath<liaison.groble.domain.user.enums.UserType> lastUserType = createEnum("lastUserType", liaison.groble.domain.user.enums.UserType.class);

    public final StringPath refreshToken = createString("refreshToken");

    public final DateTimePath<java.time.Instant> refreshTokenExpiresAt = createDateTime("refreshTokenExpiresAt", java.time.Instant.class);

    public final liaison.groble.domain.user.vo.QSellerInfo sellerInfo;

    public final QSocialAccount socialAccount;

    public final SetPath<liaison.groble.domain.terms.entity.UserTerms, liaison.groble.domain.terms.entity.QUserTerms> termsAgreements = this.<liaison.groble.domain.terms.entity.UserTerms, liaison.groble.domain.terms.entity.QUserTerms>createSet("termsAgreements", liaison.groble.domain.terms.entity.UserTerms.class, liaison.groble.domain.terms.entity.QUserTerms.class, PathInits.DIRECT2);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final liaison.groble.domain.user.vo.QUserProfile userProfile;

    public final SetPath<liaison.groble.domain.role.UserRole, liaison.groble.domain.role.QUserRole> userRoles = this.<liaison.groble.domain.role.UserRole, liaison.groble.domain.role.QUserRole>createSet("userRoles", liaison.groble.domain.role.UserRole.class, liaison.groble.domain.role.QUserRole.class, PathInits.DIRECT2);

    public final liaison.groble.domain.user.vo.QUserStatusInfo userStatusInfo;

    public final StringPath uuid = createString("uuid");

    public QUser(String variable) {
        this(User.class, forVariable(variable), INITS);
    }

    public QUser(Path<? extends User> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUser(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUser(PathMetadata metadata, PathInits inits) {
        this(User.class, metadata, inits);
    }

    public QUser(Class<? extends User> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.integratedAccount = inits.isInitialized("integratedAccount") ? new QIntegratedAccount(forProperty("integratedAccount"), inits.get("integratedAccount")) : null;
        this.sellerInfo = inits.isInitialized("sellerInfo") ? new liaison.groble.domain.user.vo.QSellerInfo(forProperty("sellerInfo")) : null;
        this.socialAccount = inits.isInitialized("socialAccount") ? new QSocialAccount(forProperty("socialAccount"), inits.get("socialAccount")) : null;
        this.userProfile = inits.isInitialized("userProfile") ? new liaison.groble.domain.user.vo.QUserProfile(forProperty("userProfile")) : null;
        this.userStatusInfo = inits.isInitialized("userStatusInfo") ? new liaison.groble.domain.user.vo.QUserStatusInfo(forProperty("userStatusInfo")) : null;
    }

}

