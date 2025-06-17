package liaison.groble.application.admin.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminOrderSummaryInfoDto {
  private Long contentId;
  private String merchantUid;
  private LocalDateTime createdAt;
  private String contentType;
  private String purchaserName;
  private String contentTitle;
  private BigDecimal finalPrice;
  private String orderStatus;
}
