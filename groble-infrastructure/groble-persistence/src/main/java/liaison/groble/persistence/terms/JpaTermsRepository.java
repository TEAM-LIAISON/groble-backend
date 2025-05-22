package liaison.groble.persistence.terms;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import liaison.groble.domain.terms.Terms;
import liaison.groble.domain.terms.enums.TermsType;

public interface JpaTermsRepository extends JpaRepository<Terms, Long> {

  @Query(
      "SELECT t FROM Terms t WHERE t.type IN :types AND t.effectiveFrom <= :now AND (t.effectiveTo > :now OR t.effectiveTo IS NULL)")
  List<Terms> findByTypeInAndEffectiveFromBeforeAndEffectiveToAfterOrEffectiveToIsNull(
      @Param("types") List<TermsType> types,
      @Param("now") LocalDateTime now,
      @Param("now") LocalDateTime nowAgain);

  @Query(
      "SELECT t FROM Terms t WHERE t.effectiveFrom <= :now AND (t.effectiveTo > :now OR t.effectiveTo IS NULL)")
  List<Terms> findByEffectiveFromBeforeAndEffectiveToAfterOrEffectiveToIsNull(
      @Param("now") LocalDateTime now, @Param("now") LocalDateTime nowAgain);

  List<Terms> findByTypeIn(List<TermsType> types);

  /** 주어진 시점(now)에 유효한, 특정 타입의 최신 약관 한 건을 조회합니다. */
  @Query(
      """
    SELECT t
      FROM Terms t
     WHERE t.type = :type
       AND t.effectiveFrom <= :now
       AND (t.effectiveTo   IS NULL
            OR t.effectiveTo >  :now)
     ORDER BY t.effectiveFrom DESC
    """)
  Optional<Terms> findLatestByTypeAndEffectiveAt(
      @Param("type") TermsType type, @Param("now") LocalDateTime now);

  @Query(
      "SELECT t FROM Terms t WHERE t.effectiveFrom <= CURRENT_TIMESTAMP "
          + "AND (t.effectiveTo IS NULL OR t.effectiveTo > CURRENT_TIMESTAMP)")
  List<Terms> findAllLatestTerms();

  @Query(
      "SELECT t FROM Terms t WHERE t.effectiveFrom <= :now "
          + "AND (t.effectiveTo IS NULL OR t.effectiveTo > :now)")
  List<Terms> findAllLatestTerms(@Param("now") LocalDateTime now);
}
