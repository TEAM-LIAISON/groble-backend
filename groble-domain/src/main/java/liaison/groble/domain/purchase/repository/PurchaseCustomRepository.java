package liaison.groble.domain.purchase.repository;

import java.util.List;

import liaison.groble.common.response.CursorResponse;
import liaison.groble.domain.content.enums.ContentType;
import liaison.groble.domain.order.entity.Order;
import liaison.groble.domain.purchase.dto.FlatPurchaseContentPreviewDTO;

public interface PurchaseCustomRepository {

  CursorResponse<FlatPurchaseContentPreviewDTO> findMyPurchasingContentsWithCursor(
      Long userId,
      Long lastContentId,
      int size,
      List<Order.OrderStatus> statusList,
      ContentType contentType);

  int countMyPurchasingContents(
      Long userId, List<Order.OrderStatus> orderStatusList, ContentType contentType);
}
