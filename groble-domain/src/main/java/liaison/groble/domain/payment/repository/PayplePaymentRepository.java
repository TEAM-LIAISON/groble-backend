package liaison.groble.domain.payment.repository;

import liaison.groble.domain.payment.entity.PayplePayment;

public interface PayplePaymentRepository {

  PayplePayment save(PayplePayment payplePayment);
}
