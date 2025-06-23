package liaison.groble.domain.user.repository;

import java.util.List;

import liaison.groble.domain.user.entity.SellerContact;
import liaison.groble.domain.user.entity.User;

public interface SellerContactRepository {
  List<SellerContact> findAllByUser(User user);
}
