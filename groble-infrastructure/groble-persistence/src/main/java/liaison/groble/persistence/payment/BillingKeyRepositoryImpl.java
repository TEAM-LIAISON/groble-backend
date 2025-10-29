package liaison.groble.persistence.payment;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import liaison.groble.domain.payment.entity.BillingKey;
import liaison.groble.domain.payment.enums.BillingKeyStatus;
import liaison.groble.domain.payment.repository.BillingKeyRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class BillingKeyRepositoryImpl implements BillingKeyRepository {

  private final JpaBillingKeyRepository jpaBillingKeyRepository;

  @Override
  public BillingKey save(BillingKey billingKey) {
    return jpaBillingKeyRepository.save(billingKey);
  }

  @Override
  public Optional<BillingKey> findByUserIdAndStatus(Long userId, BillingKeyStatus status) {
    return jpaBillingKeyRepository.findByUserIdAndStatus(userId, status);
  }

  @Override
  public List<BillingKey> findByUserId(Long userId) {
    return jpaBillingKeyRepository.findByUserId(userId);
  }
}
