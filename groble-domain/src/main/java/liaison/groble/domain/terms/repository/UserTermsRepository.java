package liaison.groble.domain.terms.repository;

public interface UserTermsRepository {

  boolean existsByUserId(Long userId);
}
