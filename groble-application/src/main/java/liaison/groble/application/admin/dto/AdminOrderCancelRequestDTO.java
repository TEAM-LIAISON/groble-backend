package liaison.groble.application.admin.dto;

import java.time.LocalDateTime;

import liaison.groble.domain.order.entity.Order;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminOrderCancelRequestDTO {
  private String merchantUid;
  private String action;
  private Order.OrderStatus resultStatus;
  private String message;
  private LocalDateTime processedAt;
}
