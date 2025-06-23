package liaison.groble.persistence.user;

import java.util.List;

import org.springframework.stereotype.Repository;

import liaison.groble.domain.user.entity.SellerContact;
import liaison.groble.domain.user.entity.User;
import liaison.groble.domain.user.repository.SellerContactRepository;

import lombok.AllArgsConstructor;

@Repository
@AllArgsConstructor
public class SellerContactRepositoryImpl implements SellerContactRepository {
  private final JpaSellerContactRepository jpaSellerContactRepository;

  @Override
  public List<SellerContact> findAllByUser(User user) {
    return jpaSellerContactRepository.findAllByUser(user);
  }
}
