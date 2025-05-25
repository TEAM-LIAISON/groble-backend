package liaison.groble.persistence.payment;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import liaison.groble.domain.payment.entity.PayplePayment;

public interface JpaPayplePaymentRepository extends JpaRepository<PayplePayment, Long> {

  PayplePayment save(PayplePayment payplePayment);

  Optional<PayplePayment> findByOrderId(String orderId);

  List<PayplePayment> findByUserId(Long userId);

  List<PayplePayment> findByUserIdAndBillingKey(Long userId, String billingKey);

  List<PayplePayment> findByBillingKey(String billingKey);
}
