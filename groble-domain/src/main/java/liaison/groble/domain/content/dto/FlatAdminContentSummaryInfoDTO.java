package liaison.groble.domain.content.dto;

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
  private LocalDateTime createdAt;
  private String contentType;
  private String sellerName;
  private String contentTitle;
  private int priceOptionLength;
  private String contentStatus;
  private String adminContentCheckingStatus;
}
