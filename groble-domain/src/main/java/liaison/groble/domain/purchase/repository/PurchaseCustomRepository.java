package liaison.groble.domain.purchase.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import liaison.groble.common.response.CursorResponse;
import liaison.groble.domain.order.entity.Order;
import liaison.groble.domain.purchase.dto.FlatContentSellDetailDTO;
import liaison.groble.domain.purchase.dto.FlatPurchaseContentPreviewDTO;
import liaison.groble.domain.purchase.dto.FlatSellManageDetailDTO;

public interface PurchaseCustomRepository {

  CursorResponse<FlatPurchaseContentPreviewDTO> findMyPurchasingContentsWithCursor(
      Long userId, Long lastContentId, int size, List<Order.OrderStatus> statusList);

  int countMyPurchasingContents(Long userId, List<Order.OrderStatus> orderStatusList);

  // 내 스토어 - 상품 관리 - 판매 관리- 판매리스트 상세
  Optional<FlatContentSellDetailDTO> getContentSellDetailDTO(
      Long userId, Long contentId, Long purchaseId);

  Page<FlatContentSellDetailDTO> getContentSellPageDTOs(
      Long userId, Long contentId, Pageable pageable);

  Optional<FlatSellManageDetailDTO> getSellManageDetail(Long userId, Long contentId);
}
