package liaison.groble.domain.user.repository;

import java.util.List;
import java.util.Optional;

import liaison.groble.domain.user.entity.UserTermsAgreement;

public interface UserTermsAgreementRepository {
  List<UserTermsAgreement> findByUserId(Long userId);

  Optional<UserTermsAgreement> findByUserIdAndTermsId(Long userId, Long termsId);

  UserTermsAgreement save(UserTermsAgreement userTermsAgreement);
}
