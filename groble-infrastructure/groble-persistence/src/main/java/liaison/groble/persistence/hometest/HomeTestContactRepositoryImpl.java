package liaison.groble.persistence.hometest;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import liaison.groble.domain.hometest.entity.HomeTestContact;
import liaison.groble.domain.hometest.repository.HomeTestContactRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class HomeTestContactRepositoryImpl implements HomeTestContactRepository {

  private final JpaHomeTestContactRepository jpaHomeTestContactRepository;

  @Override
  public Optional<HomeTestContact> findByPhoneNumber(String phoneNumber) {
    return jpaHomeTestContactRepository.findByPhoneNumber(phoneNumber);
  }

  @Override
  public HomeTestContact save(HomeTestContact contact) {
    return jpaHomeTestContactRepository.save(contact);
  }

  @Override
  public Page<HomeTestContact> findAll(Pageable pageable) {
    return jpaHomeTestContactRepository.findAll(pageable);
  }
}
