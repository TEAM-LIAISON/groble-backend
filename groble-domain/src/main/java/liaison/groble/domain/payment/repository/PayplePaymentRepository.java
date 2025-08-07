package liaison.groble.domain.payment.repository;

import java.util.Optional;

import liaison.groble.domain.payment.entity.PayplePayment;

public interface PayplePaymentRepository {

  Optional<PayplePayment> findById(Long payplePaymentId);

  PayplePayment save(PayplePayment payplePayment);

  Optional<PayplePayment> findByPcdPayOid(String pcdPayOid);
}
