package liaison.groble.application.admin.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminOrderSummaryInfoDTO {
  // 콘텐츠 ID
  private Long contentId;
  // 주문 식별 ID
  private String merchantUid;

  // 결제 날짜
  private LocalDateTime createdAt;

  // 콘텐츠 유형
  private String contentType;

  // 콘텐츠 상태
  private String contentStatus;
  private String purchaserName;
  private String contentTitle;
  private BigDecimal finalPrice;
  private String orderStatus;
  private String failureReason;
  private String paymentType;
}
