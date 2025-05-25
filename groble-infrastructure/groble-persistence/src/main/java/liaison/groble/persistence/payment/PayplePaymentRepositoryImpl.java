package liaison.groble.persistence.payment;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import liaison.groble.domain.payment.entity.PayplePayment;
import liaison.groble.domain.payment.repository.PayplePaymentRepository;

import lombok.AllArgsConstructor;

@Repository
@AllArgsConstructor
public class PayplePaymentRepositoryImpl implements PayplePaymentRepository {
  private final JpaPayplePaymentRepository jpaPayplePaymentRepository;

  @Override
  public PayplePayment save(PayplePayment payplePayment) {
    return jpaPayplePaymentRepository.save(payplePayment);
  }

  @Override
  public Optional<PayplePayment> findByOrderId(String orderId) {
    return jpaPayplePaymentRepository.findByOrderId(orderId);
  }

  @Override
  public List<PayplePayment> findByUserId(Long userId) {
    return jpaPayplePaymentRepository.findByUserId(userId);
  }

  @Override
  public List<PayplePayment> findByUserIdAndBillingKey(Long userId, String billingKey) {
    return jpaPayplePaymentRepository.findByUserIdAndBillingKey(userId, billingKey);
  }

  @Override
  public List<PayplePayment> findByBillingKey(String billingKey) {
    return jpaPayplePaymentRepository.findByBillingKey(billingKey);
  }
}
