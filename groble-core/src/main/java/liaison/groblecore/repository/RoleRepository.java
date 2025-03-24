package liaison.groblecore.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import liaison.groblecore.domain.Role;
import liaison.groblecore.domain.RoleType;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
  Optional<Role> findByName(RoleType name);
}
