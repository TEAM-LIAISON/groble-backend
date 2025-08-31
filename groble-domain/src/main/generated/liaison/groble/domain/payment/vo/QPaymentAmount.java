package liaison.groble.domain.payment.vo;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QPaymentAmount is a Querydsl query type for PaymentAmount
 */
@Generated("com.querydsl.codegen.DefaultEmbeddableSerializer")
public class QPaymentAmount extends BeanPath<PaymentAmount> {

    private static final long serialVersionUID = -433571771L;

    public static final QPaymentAmount paymentAmount = new QPaymentAmount("paymentAmount");

    public final NumberPath<java.math.BigDecimal> value = createNumber("value", java.math.BigDecimal.class);

    public final BooleanPath zero = createBoolean("zero");

    public QPaymentAmount(String variable) {
        super(PaymentAmount.class, forVariable(variable));
    }

    public QPaymentAmount(Path<? extends PaymentAmount> path) {
        super(path.getType(), path.getMetadata());
    }

    public QPaymentAmount(PathMetadata metadata) {
        super(PaymentAmount.class, metadata);
    }

}

