package liaison.groble.domain.user.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import javax.annotation.processing.Generated;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.*;

/** QSellerInfo is a Querydsl query type for SellerInfo */
@Generated("com.querydsl.codegen.DefaultEmbeddableSerializer")
public class QSellerInfo extends BeanPath<SellerInfo> {

  private static final long serialVersionUID = 2109755241L;

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

  public final EnumPath<liaison.groble.domain.user.enums.BusinessType> businessType =
      createEnum("businessType", liaison.groble.domain.user.enums.BusinessType.class);

  public final DateTimePath<java.time.LocalDateTime> lastVerificationAttempt =
      createDateTime("lastVerificationAttempt", java.time.LocalDateTime.class);

  public final StringPath representativeName = createString("representativeName");

  public final StringPath taxInvoiceEmail = createString("taxInvoiceEmail");

  public final StringPath verificationMessage = createString("verificationMessage");

  public final EnumPath<liaison.groble.domain.user.enums.SellerVerificationStatus>
      verificationStatus =
          createEnum(
              "verificationStatus",
              liaison.groble.domain.user.enums.SellerVerificationStatus.class);

  public QSellerInfo(String variable) {
    super(SellerInfo.class, forVariable(variable));
  }

  public QSellerInfo(Path<? extends SellerInfo> path) {
    super(path.getType(), path.getMetadata());
  }

  public QSellerInfo(PathMetadata metadata) {
    super(SellerInfo.class, metadata);
  }
}
