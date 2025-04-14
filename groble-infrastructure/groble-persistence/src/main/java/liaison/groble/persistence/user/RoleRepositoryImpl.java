package liaison.groble.persistence.user;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import liaison.groble.domain.user.entity.Role;
import liaison.groble.domain.user.repository.RoleRepository;

import lombok.AllArgsConstructor;

@Repository
@AllArgsConstructor
public class RoleRepositoryImpl implements RoleRepository {

  private final JpaRoleRepository jpaRoleRepository;

  @Override
  public Optional<Role> findByName(String name) {
    return jpaRoleRepository.findByName(name);
  }
}
