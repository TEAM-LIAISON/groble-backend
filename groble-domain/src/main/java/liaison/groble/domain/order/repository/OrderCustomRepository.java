package liaison.groble.domain.order.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import liaison.groble.domain.order.dto.FlatAdminOrderSummaryInfoDTO;

public interface OrderCustomRepository {
  Page<FlatAdminOrderSummaryInfoDTO> findOrdersByPageable(Pageable pageable);
}
