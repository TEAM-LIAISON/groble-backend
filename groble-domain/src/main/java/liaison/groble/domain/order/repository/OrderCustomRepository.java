package liaison.groble.domain.order.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import liaison.groble.domain.order.dto.FlatAdminOrderSummaryInfoDTO;
import liaison.groble.domain.order.entity.Order;

public interface OrderCustomRepository {
  Page<FlatAdminOrderSummaryInfoDTO> findOrdersByPageable(Pageable pageable);

  Optional<Order> findByMerchantUidAndUserId(String merchantUid, Long userId);
}
