package liaison.groble.domain.user.vo;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QIdentityVerification is a Querydsl query type for IdentityVerification
 */
@Generated("com.querydsl.codegen.DefaultEmbeddableSerializer")
public class QIdentityVerification extends BeanPath<IdentityVerification> {

    private static final long serialVersionUID = 1039515903L;

    public static final QIdentityVerification identityVerification = new QIdentityVerification("identityVerification");

    public final StringPath certificationProvider = createString("certificationProvider");

    public final StringPath certificationTxId = createString("certificationTxId");

    public final StringPath ci = createString("ci");

    public final StringPath di = createString("di");

    public final DateTimePath<java.time.LocalDateTime> expiredAt = createDateTime("expiredAt", java.time.LocalDateTime.class);

    public final EnumPath<liaison.groble.domain.user.enums.IdentityVerificationStatus> status = createEnum("status", liaison.groble.domain.user.enums.IdentityVerificationStatus.class);

    public final MapPath<String, Object, SimplePath<Object>> verificationData = this.<String, Object, SimplePath<Object>>createMap("verificationData", String.class, Object.class, SimplePath.class);

    public final EnumPath<IdentityVerification.VerificationMethod> verificationMethod = createEnum("verificationMethod", IdentityVerification.VerificationMethod.class);

    public final BooleanPath verified = createBoolean("verified");

    public final DateTimePath<java.time.LocalDateTime> verifiedAt = createDateTime("verifiedAt", java.time.LocalDateTime.class);

    public final DatePath<java.time.LocalDate> verifiedBirthDate = createDate("verifiedBirthDate", java.time.LocalDate.class);

    public final StringPath verifiedGender = createString("verifiedGender");

    public final StringPath verifiedName = createString("verifiedName");

    public final StringPath verifiedNationality = createString("verifiedNationality");

    public final StringPath verifiedPhone = createString("verifiedPhone");

    public QIdentityVerification(String variable) {
        super(IdentityVerification.class, forVariable(variable));
    }

    public QIdentityVerification(Path<? extends IdentityVerification> path) {
        super(path.getType(), path.getMetadata());
    }

    public QIdentityVerification(PathMetadata metadata) {
        super(IdentityVerification.class, metadata);
    }

}

