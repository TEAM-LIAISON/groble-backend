package liaison.groble.persistence.role;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import liaison.groble.domain.role.Role;
import liaison.groble.domain.role.repository.RoleRepository;

import lombok.AllArgsConstructor;

@Repository
@AllArgsConstructor
public class RoleRepositoryImpl implements RoleRepository {

  private final JpaRoleRepository jpaRoleRepository;

  @Override
  public Optional<Role> findByName(String name) {
    return jpaRoleRepository.findByName(name);
  }

  @Override
  public List<Role> findAll() {
    return jpaRoleRepository.findAll();
  }

  @Override
  public void saveAll(List<Role> roles) {
    jpaRoleRepository.saveAll(roles);
  }
}
