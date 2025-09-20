package liaison.groble.domain.purchase.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import liaison.groble.common.response.CursorResponse;
import liaison.groble.domain.dashboard.dto.FlatDashboardOverviewDTO;
import liaison.groble.domain.order.entity.Order;
import liaison.groble.domain.purchase.dto.FlatContentSellDetailDTO;
import liaison.groble.domain.purchase.dto.FlatPurchaseContentDetailDTO;
import liaison.groble.domain.purchase.dto.FlatPurchaseContentPreviewDTO;
import liaison.groble.domain.purchase.dto.FlatSellManageDetailDTO;

public interface PurchaseCustomRepository {

  Optional<FlatPurchaseContentDetailDTO> getPurchaseContentDetail(Long userId, String merchantUid);

  CursorResponse<FlatPurchaseContentPreviewDTO> findMyPurchasingContentsWithCursor(
      Long userId, Long lastContentId, int size, List<Order.OrderStatus> statusList);

  int countMyPurchasingContents(Long userId, List<Order.OrderStatus> orderStatusList);

  // 내 스토어 - 상품 관리 - 판매 관리- 판매리스트 상세
  Optional<FlatContentSellDetailDTO> getContentSellDetailDTO(
      Long userId, Long contentId, Long purchaseId);

  Page<FlatContentSellDetailDTO> getContentSellPageDTOs(
      Long userId, Long contentId, Pageable pageable);

  Page<FlatPurchaseContentPreviewDTO> findMyPurchasedContents(
      Long userId, List<Order.OrderStatus> orderStatuses, Pageable pageable);

  Page<FlatPurchaseContentPreviewDTO> findMyPurchasedContentsForGuest(
      String guestPhoneNumber, List<Order.OrderStatus> orderStatuses, Pageable pageable);

  Optional<FlatSellManageDetailDTO> getSellManageDetail(Long userId, Long contentId);

  boolean existsByUserAndContent(Long userId, Long contentId);

  boolean existsByGuestUserAndContent(Long guestUserId, Long contentId);

  FlatDashboardOverviewDTO getDashboardOverviewStats(Long sellerId);

  FlatDashboardOverviewDTO getAdminDashboardOverviewStats();

  Optional<FlatPurchaseContentDetailDTO> getPurchaseContentDetailForGuest(String merchantUid);
}
