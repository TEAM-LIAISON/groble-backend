package liaison.groble.domain.payment.repository;

import liaison.groble.domain.payment.entity.Payment;

public interface PaymentRepository {
  Payment save(Payment payment);
}
