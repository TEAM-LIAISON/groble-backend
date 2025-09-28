package liaison.groble.persistence.hometest;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import liaison.groble.domain.hometest.entity.HomeTestContact;

public interface JpaHomeTestContactRepository extends JpaRepository<HomeTestContact, Long> {

  Optional<HomeTestContact> findByPhoneNumber(String phoneNumber);
}
