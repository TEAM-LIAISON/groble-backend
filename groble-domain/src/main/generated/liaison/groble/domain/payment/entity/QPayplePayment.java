package liaison.groble.domain.payment.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QPayplePayment is a Querydsl query type for PayplePayment
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPayplePayment extends EntityPathBase<PayplePayment> {

    private static final long serialVersionUID = -1282774410L;

    public static final QPayplePayment payplePayment = new QPayplePayment("payplePayment");

    public final liaison.groble.domain.common.entity.QBaseTimeEntity _super = new liaison.groble.domain.common.entity.QBaseTimeEntity(this);

    public final DateTimePath<java.time.LocalDateTime> canceledAt = createDateTime("canceledAt", java.time.LocalDateTime.class);

    public final StringPath cancelReason = createString("cancelReason");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath failReason = createString("failReason");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final DateTimePath<java.time.LocalDateTime> paymentDate = createDateTime("paymentDate", java.time.LocalDateTime.class);

    public final StringPath pcdEasyPayMethod = createString("pcdEasyPayMethod");

    public final StringPath pcdPayAuthKey = createString("pcdPayAuthKey");

    public final StringPath pcdPayCardAuthNo = createString("pcdPayCardAuthNo");

    public final StringPath pcdPayCardName = createString("pcdPayCardName");

    public final StringPath pcdPayCardNum = createString("pcdPayCardNum");

    public final StringPath pcdPayCardQuota = createString("pcdPayCardQuota");

    public final StringPath pcdPayCardReceipt = createString("pcdPayCardReceipt");

    public final StringPath pcdPayCardTradeNum = createString("pcdPayCardTradeNum");

    public final StringPath pcdPayCardVer = createString("pcdPayCardVer");

    public final StringPath pcdPayCode = createString("pcdPayCode");

    public final StringPath pcdPayCofUrl = createString("pcdPayCofUrl");

    public final StringPath pcdPayerEmail = createString("pcdPayerEmail");

    public final StringPath pcdPayerHp = createString("pcdPayerHp");

    public final StringPath pcdPayerId = createString("pcdPayerId");

    public final StringPath pcdPayerName = createString("pcdPayerName");

    public final StringPath pcdPayerNo = createString("pcdPayerNo");

    public final StringPath pcdPayGoods = createString("pcdPayGoods");

    public final StringPath pcdPayHost = createString("pcdPayHost");

    public final StringPath pcdPayIsTax = createString("pcdPayIsTax");

    public final StringPath pcdPayMonth = createString("pcdPayMonth");

    public final StringPath pcdPayMsg = createString("pcdPayMsg");

    public final StringPath pcdPayOid = createString("pcdPayOid");

    public final StringPath pcdPayReqKey = createString("pcdPayReqKey");

    public final StringPath pcdPayRst = createString("pcdPayRst");

    public final StringPath pcdPayTaxTotal = createString("pcdPayTaxTotal");

    public final StringPath pcdPayTime = createString("pcdPayTime");

    public final StringPath pcdPayTotal = createString("pcdPayTotal");

    public final StringPath pcdPayType = createString("pcdPayType");

    public final StringPath pcdPayWork = createString("pcdPayWork");

    public final StringPath pcdPayYear = createString("pcdPayYear");

    public final StringPath pcdRegulerFlag = createString("pcdRegulerFlag");

    public final StringPath pcdRstUrl = createString("pcdRstUrl");

    public final StringPath pcdSimpleFlag = createString("pcdSimpleFlag");

    public final StringPath pcdUserDefine1 = createString("pcdUserDefine1");

    public final StringPath pcdUserDefine2 = createString("pcdUserDefine2");

    public final EnumPath<liaison.groble.domain.payment.enums.PayplePaymentStatus> status = createEnum("status", liaison.groble.domain.payment.enums.PayplePaymentStatus.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QPayplePayment(String variable) {
        super(PayplePayment.class, forVariable(variable));
    }

    public QPayplePayment(Path<? extends PayplePayment> path) {
        super(path.getType(), path.getMetadata());
    }

    public QPayplePayment(PathMetadata metadata) {
        super(PayplePayment.class, metadata);
    }

}

