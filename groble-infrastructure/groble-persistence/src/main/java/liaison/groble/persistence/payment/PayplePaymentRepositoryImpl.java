package liaison.groble.persistence.payment;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import liaison.groble.domain.payment.entity.PayplePayment;
import liaison.groble.domain.payment.enums.PayplePaymentStatus;
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
  public Optional<PayplePayment> findByPcdPayOid(String pcdPayOid) {
    return jpaPayplePaymentRepository.findByPcdPayOid(pcdPayOid);
  }

  @Override
  public List<PayplePayment> findByPcdPayerNoAndStatus(
      String pcdPayerNo, PayplePaymentStatus status) {
    return jpaPayplePaymentRepository.findByPcdPayerNoAndStatus(pcdPayerNo, status);
  }
}
