package liaison.groble.application.settlement.policy;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.domain.settlement.entity.FeePolicy;
import liaison.groble.domain.settlement.enums.FeePolicyScope;
import liaison.groble.domain.settlement.repository.FeePolicyRepository;
import liaison.groble.domain.settlement.vo.FeePolicySnapshot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeePolicyService {

  private final FeePolicyRepository feePolicyRepository;
  private final FeePolicyDefaultsProperties defaultsProperties;

  @Transactional(readOnly = true)
  public FeePolicySnapshot resolveForSeller(Long sellerId) {
    LocalDateTime now = LocalDateTime.now();

    return findEffectivePolicy(FeePolicyScope.SELLER, sellerId, now)
        .or(() -> findEffectivePolicy(FeePolicyScope.GLOBAL, null, now))
        .map(FeePolicy::toSnapshot)
        .orElseGet(this::defaultSnapshot);
  }

  private Optional<FeePolicy> findEffectivePolicy(
      FeePolicyScope scope, Long scopeReference, LocalDateTime target) {
    List<FeePolicy> policies =
        feePolicyRepository.findEffectivePolicies(scope, scopeReference, target);
    return policies.stream().findFirst();
  }

  public FeePolicySnapshot defaultSnapshot() {
    return new FeePolicySnapshot(
        defaultsProperties.getPlatformFeeRateApplied(),
        defaultsProperties.getPlatformFeeRateDisplay(),
        defaultsProperties.getPlatformFeeRateBaseline(),
        defaultsProperties.getPgFeeRateApplied(),
        defaultsProperties.getPgFeeRateDisplay(),
        defaultsProperties.getPgFeeRateBaseline(),
        defaultsProperties.getVatRate());
  }
}
