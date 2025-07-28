package liaison.groble.persistence.payment;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import liaison.groble.domain.payment.entity.Payment;
import liaison.groble.domain.payment.repository.PaymentRepository;

import lombok.AllArgsConstructor;

@Repository
@AllArgsConstructor
public class PaymentRepositoryImpl implements PaymentRepository {
  private final JpaPaymentRepository jpaPaymentRepository;

  @Override
  public Payment save(Payment payment) {
    return jpaPaymentRepository.save(payment);
  }

  @Override
  public Optional<Payment> findByOrderId(Long orderId) {
    return jpaPaymentRepository.findByOrderId(orderId);
  }

  @Override
  public Optional<Payment> findById(Long paymentId) {
    return jpaPaymentRepository.findById(paymentId);
  }
}
