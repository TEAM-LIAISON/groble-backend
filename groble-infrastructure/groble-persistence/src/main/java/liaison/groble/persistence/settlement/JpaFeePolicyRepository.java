package liaison.groble.persistence.settlement;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import liaison.groble.domain.settlement.entity.FeePolicy;
import liaison.groble.domain.settlement.enums.FeePolicyScope;

public interface JpaFeePolicyRepository extends JpaRepository<FeePolicy, Long> {

  @Query(
      """
      SELECT fp
        FROM FeePolicy fp
       WHERE fp.scopeType = :scopeType
         AND ((:scopeReference IS NULL AND fp.scopeReference IS NULL)
              OR fp.scopeReference = :scopeReference)
         AND fp.effectiveFrom <= :target
         AND (fp.effectiveTo IS NULL OR fp.effectiveTo > :target)
       ORDER BY fp.effectiveFrom DESC
      """)
  List<FeePolicy> findEffectivePolicies(
      @Param("scopeType") FeePolicyScope scopeType,
      @Param("scopeReference") Long scopeReference,
      @Param("target") LocalDateTime target);
}
