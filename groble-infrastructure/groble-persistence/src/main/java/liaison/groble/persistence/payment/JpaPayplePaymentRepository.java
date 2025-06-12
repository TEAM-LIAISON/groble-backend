package liaison.groble.persistence.payment;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import liaison.groble.domain.payment.entity.PayplePayment;
import liaison.groble.domain.payment.enums.PayplePaymentStatus;

public interface JpaPayplePaymentRepository extends JpaRepository<PayplePayment, Long> {

  PayplePayment save(PayplePayment payplePayment);

  Optional<PayplePayment> findByPcdPayOid(String pcdPayOid);

  List<PayplePayment> findByPcdPayerNoAndStatus(String pcdPayerNo, PayplePaymentStatus status);
}
