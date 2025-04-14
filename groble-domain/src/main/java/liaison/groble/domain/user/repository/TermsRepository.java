package liaison.groble.domain.user.repository;

import java.util.List;
import java.util.Optional;

import liaison.groble.domain.user.entity.Terms;
import liaison.groble.domain.user.enums.TermsType;

public interface TermsRepository {
  List<Terms> findActiveTermsByTypes(List<TermsType> termsTypes);

  List<Terms> findByTypesIn(List<TermsType> termsTypes);

  List<Terms> findActiveTerms();

  Optional<Terms> findById(Long id);

  Terms save(Terms terms);
}
