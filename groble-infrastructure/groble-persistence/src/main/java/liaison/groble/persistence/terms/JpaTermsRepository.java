package liaison.groble.persistence.terms;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import liaison.groble.domain.user.entity.Terms;
import liaison.groble.domain.user.enums.TermsType;

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

  @Query(
      "SELECT t FROM Terms t WHERE t.type = :type AND t.effectiveTo IS NULL ORDER BY t.effectiveFrom DESC")
  Optional<Terms> findTopByTypeAndEffectiveToIsNullOrderByEffectiveFromDesc(
      @Param("type") TermsType type);
}
