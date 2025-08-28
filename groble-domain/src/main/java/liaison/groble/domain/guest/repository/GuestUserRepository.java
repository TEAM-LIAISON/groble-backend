package liaison.groble.domain.guest.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import liaison.groble.domain.guest.entity.GuestUser;

public interface GuestUserRepository extends JpaRepository<GuestUser, Long> {

  Optional<GuestUser> findByPhoneNumber(String phone);

  Optional<GuestUser> findByEmail(String email);

  boolean existsByPhoneNumber(String phone);

  boolean existsByEmail(String email);
}
