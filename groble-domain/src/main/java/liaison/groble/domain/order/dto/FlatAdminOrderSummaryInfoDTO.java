package liaison.groble.domain.order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlatAdminOrderSummaryInfoDTO {
  private LocalDateTime createdAt;
  private String contentType;
  private String purchaserName;
  private String contentTitle;
  private BigDecimal finalPrice;
  private String orderStatus;
}
