package liaison.groble.persistence.payment;

import org.springframework.stereotype.Repository;

import liaison.groble.domain.payment.entity.PaymentCancel;
import liaison.groble.domain.payment.repository.PaymentCancelRepository;

import lombok.AllArgsConstructor;

@Repository
@AllArgsConstructor
public class PaymentCancelRepositoryImpl implements PaymentCancelRepository {
  private final JpaPaymentCancelRepository jpaPaymentCancelRepository;

  @Override
  public PaymentCancel save(PaymentCancel paymentCancel) {
    return jpaPaymentCancelRepository.save(paymentCancel);
  }
}
