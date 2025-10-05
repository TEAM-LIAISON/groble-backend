package liaison.groble.domain.purchase.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlatTopContentStatDTO {
  private Long contentId;
  private String contentTitle;
  private Long sellerId;
  private String sellerNickname;
  private BigDecimal totalRevenue;
  private Long totalSalesCount;
}
