package liaison.groble.domain.payment.repository;

import java.util.List;
import java.util.Optional;

import liaison.groble.domain.payment.entity.PayplePayment;
import liaison.groble.domain.payment.enums.PayplePaymentStatus;

public interface PayplePaymentRepository {

  PayplePayment save(PayplePayment payplePayment);

  Optional<PayplePayment> findByPcdPayOid(String pcdPayOid);

  List<PayplePayment> findByPcdPayerNoAndStatus(String pcdPayerNo, PayplePaymentStatus status);
}
