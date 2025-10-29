package liaison.groble.domain.payment.repository;

import java.util.List;
import java.util.Optional;

import liaison.groble.domain.payment.entity.BillingKey;
import liaison.groble.domain.payment.enums.BillingKeyStatus;

public interface BillingKeyRepository {
  BillingKey save(BillingKey billingKey);

  Optional<BillingKey> findByUserIdAndStatus(Long userId, BillingKeyStatus status);

  List<BillingKey> findByUserId(Long userId);
}
