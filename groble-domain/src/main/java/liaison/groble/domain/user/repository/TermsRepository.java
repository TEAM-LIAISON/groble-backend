package liaison.groble.domain.user.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import liaison.groble.domain.user.entity.Terms;
import liaison.groble.domain.user.enums.TermsType;

@Repository
public interface TermsRepository extends JpaRepository<Terms, Long> {
  @Query("SELECT t FROM Terms t WHERE t.type = :type AND t.effectiveTo IS NULL")
  Optional<Terms> findLatestByType(TermsType type);

  @Query("SELECT t FROM Terms t WHERE t.effectiveTo IS NULL")
  List<Terms> findAllLatestEffective();
}
