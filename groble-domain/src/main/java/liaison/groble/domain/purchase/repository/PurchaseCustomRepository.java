package liaison.groble.domain.purchase.repository;

import java.util.List;

import liaison.groble.common.response.CursorResponse;
import liaison.groble.domain.content.enums.ContentType;
import liaison.groble.domain.purchase.dto.FlatPurchaseContentPreviewDTO;
import liaison.groble.domain.purchase.enums.PurchaseStatus;

public interface PurchaseCustomRepository {

  CursorResponse<FlatPurchaseContentPreviewDTO> findMyPurchasingContentsWithCursor(
      Long userId,
      Long lastContentId,
      int size,
      List<PurchaseStatus> statusList,
      ContentType contentType);

  int countMyPurchasingContents(
      Long userId, List<PurchaseStatus> purchaseStatusList, ContentType contentType);
}
