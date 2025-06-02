package liaison.groble.domain.terms.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import liaison.groble.domain.terms.entity.OrderTerms;
import liaison.groble.domain.terms.enums.OrderTermsType;

public interface OrderTermsRepository {
  List<OrderTerms> findAll();

  List<OrderTerms> findActiveOrderTermsByTypes(List<OrderTermsType> orderTermsTypes);

  List<OrderTerms> findByTypesIn(List<OrderTermsType> orderTermsTypes);

  List<OrderTerms> findActiveOrderTerms();

  Optional<OrderTerms> findById(Long id);

  OrderTerms save(OrderTerms orderTerms);

  Optional<OrderTerms> findLatestByTypeAndEffectiveAt(OrderTermsType type, LocalDateTime now);

  void saveAll(List<OrderTerms> orderTerms);

  /** 현재 유효한(effectiveTo가 null인) 모든 최신 약관 조회 회원가입 및 약관 동의 처리 시 사용 */
  List<OrderTerms> findAllLatestOrderTerms();

  List<OrderTerms> findAllLatestOrderTerms(LocalDateTime now);
}
