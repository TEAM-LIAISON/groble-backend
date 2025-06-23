package liaison.groble.application.sell;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.domain.user.entity.SellerContact;
import liaison.groble.domain.user.entity.User;
import liaison.groble.domain.user.repository.SellerContactRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SellerContactReader {
  private final SellerContactRepository sellerContactRepository;

  public List<SellerContact> getContactsByUser(User user) {
    return sellerContactRepository.findAllByUser(user);
  }
}
