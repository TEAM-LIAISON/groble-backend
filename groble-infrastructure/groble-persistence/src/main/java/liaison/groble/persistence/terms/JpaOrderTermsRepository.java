package liaison.groble.persistence.terms;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import liaison.groble.domain.terms.entity.OrderTerms;
import liaison.groble.domain.terms.enums.OrderTermsType;

public interface JpaOrderTermsRepository extends JpaRepository<OrderTerms, Long> {

  @Query(
      "SELECT ot FROM OrderTerms ot WHERE ot.type IN :types AND ot.effectiveFrom <= :now AND (ot.effectiveTo > :now OR ot.effectiveTo IS NULL)")
  List<OrderTerms> findByTypeInAndEffectiveFromBeforeAndEffectiveToAfterOrEffectiveToIsNull(
      @Param("types") List<OrderTermsType> types,
      @Param("now") LocalDateTime now,
      @Param("now") LocalDateTime nowAgain);

  @Query(
      "SELECT ot FROM OrderTerms ot WHERE ot.effectiveFrom <= :now AND (ot.effectiveTo > :now OR ot.effectiveTo IS NULL)")
  List<OrderTerms> findByEffectiveFromBeforeAndEffectiveToAfterOrEffectiveToIsNull(
      @Param("now") LocalDateTime now, @Param("now") LocalDateTime nowAgain);

  List<OrderTerms> findByTypeIn(List<OrderTermsType> types);

  /** 주어진 시점(now)에 유효한, 특정 타입의 최신 약관 한 건을 조회합니다. */
  @Query(
      """
          SELECT ot
            FROM OrderTerms ot
           WHERE ot.type = :type
             AND ot.effectiveFrom <= :now
             AND (ot.effectiveTo   IS NULL
                  OR ot.effectiveTo >  :now)
           ORDER BY ot.effectiveFrom DESC
          """)
  Optional<OrderTerms> findLatestByTypeAndEffectiveAt(
      @Param("type") OrderTermsType type, @Param("now") LocalDateTime now);

  @Query(
      "SELECT ot FROM OrderTerms ot WHERE ot.effectiveFrom <= CURRENT_TIMESTAMP "
          + "AND (ot.effectiveTo IS NULL OR ot.effectiveTo > CURRENT_TIMESTAMP)")
  List<OrderTerms> findAllLatestOrderTerms();

  @Query(
      "SELECT ot FROM OrderTerms ot WHERE ot.effectiveFrom <= :now "
          + "AND (ot.effectiveTo IS NULL OR ot.effectiveTo > :now)")
  List<OrderTerms> findAllLatestOrderTerms(@Param("now") LocalDateTime now);
}
