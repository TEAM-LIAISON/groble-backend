package liaison.groble.application.content.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ContentCardDto {
  private Long contentId;
  private LocalDateTime createdAt;
  private String title;
  private String thumbnailUrl;
  private String sellerName;
  private BigDecimal lowestPrice;
  private String status;
}
