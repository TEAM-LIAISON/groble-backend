package liaison.groble.domain.payment.repository;

import java.util.Optional;

import liaison.groble.domain.payment.entity.Payment;

public interface PaymentRepository {
  Payment save(Payment payment);

  Optional<Payment> findByOrderId(Long orderId);

  Optional<Payment> findById(Long paymentId);
}
