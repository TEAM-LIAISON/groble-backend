package liaison.groble.domain.guest.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import liaison.groble.domain.guest.entity.GuestUser;

public interface GuestUserRepository extends JpaRepository<GuestUser, Long> {

  Optional<GuestUser> findByPhone(String phone);

  Optional<GuestUser> findByEmail(String email);

  boolean existsByPhone(String phone);

  boolean existsByEmail(String email);
}
