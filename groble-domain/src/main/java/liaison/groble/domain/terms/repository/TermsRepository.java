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

  /** 현재 유효한(effectiveTo가 null인) 모든 최신 약관 조회 회원가입 및 약관 동의 처리 시 사용 */
  List<Terms> findAllLatestTerms();
}
