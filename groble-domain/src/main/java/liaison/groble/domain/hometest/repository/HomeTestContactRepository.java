package liaison.groble.domain.hometest.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import liaison.groble.domain.hometest.entity.HomeTestContact;

public interface HomeTestContactRepository {

  Optional<HomeTestContact> findByPhoneNumber(String phoneNumber);

  HomeTestContact save(HomeTestContact contact);

  Page<HomeTestContact> findAll(Pageable pageable);
}
