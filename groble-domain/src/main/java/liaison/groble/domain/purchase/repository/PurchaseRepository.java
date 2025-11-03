package liaison.groble.domain.purchase.repository;

import java.util.List;
import java.util.Optional;

import liaison.groble.domain.purchase.entity.Purchase;

public interface PurchaseRepository {
  Optional<Purchase> findById(Long purchaseId);

  Purchase save(Purchase purchase);

  Optional<Purchase> findByOrderId(Long orderId);

  Optional<Purchase> findByMerchantUidAndUserIdWithOrderAndContent(String merchantUid, Long userId);

  Optional<Purchase> findByMerchantUidAndGuestUserIdWithOrderAndContent(
      String merchantUid, Long guestUserId);

  List<Purchase> findByUserIdAndContentId(Long userId, Long contentId);
}
