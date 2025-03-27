package liaison.groblecore.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import liaison.groblecore.domain.AuthMethod;

@Repository
public interface AuthMethodRepository extends JpaRepository<AuthMethod, Long> {
  Optional<AuthMethod> findByUser_Id(final Long userId);
}
