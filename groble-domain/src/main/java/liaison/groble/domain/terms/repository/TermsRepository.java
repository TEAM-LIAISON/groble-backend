package liaison.groble.domain.terms.repository;

import java.util.List;
import java.util.Optional;

import liaison.groble.domain.terms.Terms;
import liaison.groble.domain.terms.enums.TermsType;

public interface TermsRepository {
  List<Terms> findAll();

  List<Terms> findActiveTermsByTypes(List<TermsType> termsTypes);

  List<Terms> findByTypesIn(List<TermsType> termsTypes);

  List<Terms> findActiveTerms();

  Optional<Terms> findById(Long id);

  Terms save(Terms terms);

  Optional<Terms> findTopByTypeAndEffectiveToIsNullOrderByEffectiveFromDesc(TermsType type);

  void saveAll(List<Terms> terms);
}
