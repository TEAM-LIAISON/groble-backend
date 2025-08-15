package liaison.groble.domain.user.repository;

import java.util.List;
import java.util.Optional;

import liaison.groble.domain.user.entity.SellerContact;
import liaison.groble.domain.user.entity.User;
import liaison.groble.domain.user.enums.ContactType;

public interface SellerContactRepository {
  List<SellerContact> findAllByUser(User user);

  SellerContact save(SellerContact sellerContact);

  Optional<SellerContact> findByUserAndContactType(User user, ContactType contactType);

  void deleteAllByUser(User user);
}
