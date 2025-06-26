package liaison.groble.persistence.terms;

public interface UserTermsCustomRepository {
  long deleteAllByUserId(Long userId);
}
