package liaison.groble.persistence.payment;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import liaison.groble.domain.payment.entity.BillingKey;
import liaison.groble.domain.payment.enums.BillingKeyStatus;

public interface JpaBillingKeyRepository extends JpaRepository<BillingKey, Long> {
  Optional<BillingKey> findByUserIdAndStatus(Long userId, BillingKeyStatus status);

  List<BillingKey> findByUserId(Long userId);

  Optional<BillingKey> findByBillingKey(String billingKey);
}
