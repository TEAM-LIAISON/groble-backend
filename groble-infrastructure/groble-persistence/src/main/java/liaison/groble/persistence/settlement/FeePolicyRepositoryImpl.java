package liaison.groble.persistence.settlement;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Repository;

import liaison.groble.domain.settlement.entity.FeePolicy;
import liaison.groble.domain.settlement.enums.FeePolicyScope;
import liaison.groble.domain.settlement.repository.FeePolicyRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class FeePolicyRepositoryImpl implements FeePolicyRepository {

  private final JpaFeePolicyRepository jpaFeePolicyRepository;

  @Override
  public List<FeePolicy> findEffectivePolicies(
      FeePolicyScope scopeType, Long scopeReference, LocalDateTime target) {
    return jpaFeePolicyRepository.findEffectivePolicies(scopeType, scopeReference, target);
  }

  @Override
  public FeePolicy save(FeePolicy feePolicy) {
    return jpaFeePolicyRepository.save(feePolicy);
  }
}
