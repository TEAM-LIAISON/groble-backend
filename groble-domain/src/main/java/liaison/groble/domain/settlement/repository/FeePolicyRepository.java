package liaison.groble.domain.settlement.repository;

import java.time.LocalDateTime;
import java.util.List;

import liaison.groble.domain.settlement.entity.FeePolicy;
import liaison.groble.domain.settlement.enums.FeePolicyScope;

public interface FeePolicyRepository {

  List<FeePolicy> findEffectivePolicies(
      FeePolicyScope scopeType, Long scopeReference, LocalDateTime target);

  FeePolicy save(FeePolicy feePolicy);
}
