package liaison.groblecore.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import liaison.groblecore.domain.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, UserRepositoryCustom {

  Optional<User> findByEmail(String email);
}
