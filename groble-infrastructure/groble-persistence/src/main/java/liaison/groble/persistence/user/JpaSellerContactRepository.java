package liaison.groble.persistence.user;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import liaison.groble.domain.user.entity.SellerContact;
import liaison.groble.domain.user.entity.User;

public interface JpaSellerContactRepository extends JpaRepository<SellerContact, Long> {
  List<SellerContact> findAllByUser(User user);
}
