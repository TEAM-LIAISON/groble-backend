package liaison.groblecore.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import liaison.groblecore.domain.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, UserRepositoryCustom {

  /**
   * 이메일로 사용자를 조회합니다.
   *
   * @param email 사용자 이메일
   * @return 사용자 Optional 객체
   */
  Optional<User> findByEmail(String email);

  /**
   * 이메일 존재 여부를 확인합니다.
   *
   * @param email 사용자 이메일
   * @return 존재하면 true, 아니면 false
   */
  Boolean existsByEmail(String email);
}
