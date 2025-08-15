package liaison.groble.persistence.user;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import liaison.groble.domain.user.entity.SellerContact;
import liaison.groble.domain.user.entity.User;
import liaison.groble.domain.user.enums.ContactType;

public interface JpaSellerContactRepository extends JpaRepository<SellerContact, Long> {
  List<SellerContact> findAllByUser(User user);

  Optional<SellerContact> findByUserAndContactType(User user, ContactType contactType);

  void deleteAllByUser(User user);
}
