package liaison.groblecore.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import liaison.groblecore.domain.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, UserRepositoryCustom {
  /**
   * 이메일로 사용자를 찾습니다.
   *
   * @param email 사용자 이메일
   * @return 조회된 사용자 (Optional)
   */
  Optional<User> findByEmail(String email);
}
