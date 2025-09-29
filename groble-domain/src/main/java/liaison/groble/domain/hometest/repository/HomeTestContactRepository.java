package liaison.groble.domain.hometest.repository;

import java.util.Optional;

import liaison.groble.domain.hometest.entity.HomeTestContact;

public interface HomeTestContactRepository {

  Optional<HomeTestContact> findByPhoneNumber(String phoneNumber);

  HomeTestContact save(HomeTestContact contact);
}
