package liaison.groble.domain.user.repository;

import java.util.List;
import java.util.Optional;

import liaison.groble.domain.user.entity.Role;

public interface RoleRepository {
  Optional<Role> findByName(String name);

  List<Role> findAll();

  void saveAll(List<Role> roles);
}
