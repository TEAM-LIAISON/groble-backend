package liaison.groble.persistence.role;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import liaison.groble.domain.user.entity.Role;

public interface JpaRoleRepository extends JpaRepository<Role, Long> {
  Optional<Role> findByName(String name);
}
