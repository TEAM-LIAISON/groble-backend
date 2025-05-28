package liaison.groble.persistence.terms;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import liaison.groble.domain.terms.entity.OrderTerms;
import liaison.groble.domain.terms.enums.OrderTermsType;
import liaison.groble.domain.terms.repository.OrderTermsRepository;

import lombok.AllArgsConstructor;

@Repository
@AllArgsConstructor
public class OrderTermsRepositoryImpl implements OrderTermsRepository {

  private final JpaOrderTermsRepository jpaOrderTermsRepository;

  @Override
  public List<OrderTerms> findAll() {
    return jpaOrderTermsRepository.findAll();
  }

  @Override
  public List<OrderTerms> findActiveOrderTermsByTypes(List<OrderTermsType> orderTermsTypes) {
    return jpaOrderTermsRepository
        .findByTypeInAndEffectiveFromBeforeAndEffectiveToAfterOrEffectiveToIsNull(
            orderTermsTypes, LocalDateTime.now(), LocalDateTime.now());
  }

  @Override
  public List<OrderTerms> findActiveOrderTerms() {
    return jpaOrderTermsRepository.findByEffectiveFromBeforeAndEffectiveToAfterOrEffectiveToIsNull(
        LocalDateTime.now(), LocalDateTime.now());
  }

  @Override
  public List<OrderTerms> findByTypesIn(List<OrderTermsType> types) {
    return jpaOrderTermsRepository.findByTypeIn(types);
  }

  @Override
  public OrderTerms save(OrderTerms orderTerms) {
    return jpaOrderTermsRepository.save(orderTerms);
  }

  @Override
  public Optional<OrderTerms> findById(Long id) {
    return jpaOrderTermsRepository.findById(id);
  }

  @Override
  public Optional<OrderTerms> findLatestByTypeAndEffectiveAt(
      OrderTermsType type, LocalDateTime now) {
    return jpaOrderTermsRepository.findLatestByTypeAndEffectiveAt(type, now);
  }

  @Override
  public void saveAll(List<OrderTerms> orderTerms) {
    jpaOrderTermsRepository.saveAll(orderTerms);
  }

  @Override
  public List<OrderTerms> findAllLatestOrderTerms() {
    return jpaOrderTermsRepository.findAllLatestOrderTerms();
  }

  @Override
  public List<OrderTerms> findAllLatestOrderTerms(LocalDateTime now) {
    return jpaOrderTermsRepository.findAllLatestOrderTerms(now);
  }
}
