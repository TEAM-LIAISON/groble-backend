package liaison.groble.domain.content.dto;

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
public class FlatAdminContentSummaryInfoDTO {
  private Long contentId;
  private LocalDateTime createdAt;
  private String contentType;
  private String sellerName;
  private String contentTitle;
  private int priceOptionLength;
  private BigDecimal minPrice;
  private String contentStatus;
  private String adminContentCheckingStatus;
}
