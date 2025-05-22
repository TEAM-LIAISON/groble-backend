package liaison.groble.persistence.terms;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import liaison.groble.domain.terms.Terms;
import liaison.groble.domain.terms.enums.TermsType;
import liaison.groble.domain.terms.repository.TermsRepository;

import lombok.AllArgsConstructor;

@Repository
@AllArgsConstructor
public class TermsRepositoryImpl implements TermsRepository {
  private final JpaTermsRepository jpaTermsRepository;

  @Override
  public List<Terms> findAll() {
    return jpaTermsRepository.findAll();
  }

  @Override
  public List<Terms> findActiveTermsByTypes(List<TermsType> termsTypes) {
    return jpaTermsRepository
        .findByTypeInAndEffectiveFromBeforeAndEffectiveToAfterOrEffectiveToIsNull(
            termsTypes, LocalDateTime.now(), LocalDateTime.now());
  }

  @Override
  public List<Terms> findActiveTerms() {
    return jpaTermsRepository.findByEffectiveFromBeforeAndEffectiveToAfterOrEffectiveToIsNull(
        LocalDateTime.now(), LocalDateTime.now());
  }

  @Override
  public List<Terms> findByTypesIn(List<TermsType> types) {
    return jpaTermsRepository.findByTypeIn(types);
  }

  @Override
  public Terms save(Terms terms) {
    return jpaTermsRepository.save(terms);
  }

  @Override
  public Optional<Terms> findById(Long id) {
    return jpaTermsRepository.findById(id);
  }

  @Override
  public Optional<Terms> findLatestByTypeAndEffectiveAt(TermsType type, LocalDateTime now) {
    return jpaTermsRepository.findLatestByTypeAndEffectiveAt(type, now);
  }

  @Override
  public void saveAll(List<Terms> terms) {
    jpaTermsRepository.saveAll(terms);
  }

  @Override
  public List<Terms> findAllLatestTerms() {
    return jpaTermsRepository.findAllLatestTerms();
  }

  @Override
  public List<Terms> findAllLatestTerms(LocalDateTime now) {
    return jpaTermsRepository.findAllLatestTerms(now);
  }
}
