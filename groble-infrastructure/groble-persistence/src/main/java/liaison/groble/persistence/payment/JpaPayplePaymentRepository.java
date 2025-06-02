package liaison.groble.persistence.payment;

import org.springframework.data.jpa.repository.JpaRepository;

import liaison.groble.domain.payment.entity.PayplePayment;

public interface JpaPayplePaymentRepository extends JpaRepository<PayplePayment, Long> {

  PayplePayment save(PayplePayment payplePayment);
}
