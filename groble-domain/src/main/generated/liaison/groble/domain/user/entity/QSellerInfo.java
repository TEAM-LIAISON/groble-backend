package liaison.groble.domain.user.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSellerInfo is a Querydsl query type for SellerInfo
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSellerInfo extends EntityPathBase<SellerInfo> {

    private static final long serialVersionUID = 2109755241L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QSellerInfo sellerInfo = new QSellerInfo("sellerInfo");

    public final StringPath bankAccountNumber = createString("bankAccountNumber");

    public final StringPath bankAccountOwner = createString("bankAccountOwner");

    public final StringPath bankName = createString("bankName");

    public final StringPath businessAddress = createString("businessAddress");

    public final StringPath businessCategory = createString("businessCategory");

    public final StringPath businessLicenseFileUrl = createString("businessLicenseFileUrl");

    public final StringPath businessName = createString("businessName");

    public final StringPath businessNumber = createString("businessNumber");

    public final StringPath businessSector = createString("businessSector");

    public final BooleanPath businessSellerRequest = createBoolean("businessSellerRequest");

    public final EnumPath<liaison.groble.domain.user.enums.BusinessType> businessType = createEnum("businessType", liaison.groble.domain.user.enums.BusinessType.class);

    public final StringPath copyOfBankbookUrl = createString("copyOfBankbookUrl");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isBusinessSeller = createBoolean("isBusinessSeller");

    public final DateTimePath<java.time.LocalDateTime> lastVerificationAttempt = createDateTime("lastVerificationAttempt", java.time.LocalDateTime.class);

    public final StringPath representativeName = createString("representativeName");

    public final StringPath taxInvoiceEmail = createString("taxInvoiceEmail");

    public final QUser user;

    public final StringPath verificationMessage = createString("verificationMessage");

    public final EnumPath<liaison.groble.domain.user.enums.SellerVerificationStatus> verificationStatus = createEnum("verificationStatus", liaison.groble.domain.user.enums.SellerVerificationStatus.class);

    public QSellerInfo(String variable) {
        this(SellerInfo.class, forVariable(variable), INITS);
    }

    public QSellerInfo(Path<? extends SellerInfo> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QSellerInfo(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QSellerInfo(PathMetadata metadata, PathInits inits) {
        this(SellerInfo.class, metadata, inits);
    }

    public QSellerInfo(Class<? extends SellerInfo> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new QUser(forProperty("user"), inits.get("user")) : null;
    }

}

