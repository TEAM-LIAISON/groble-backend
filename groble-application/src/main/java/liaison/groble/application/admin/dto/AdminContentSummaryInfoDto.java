package liaison.groble.application.admin.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminContentSummaryInfoDto {
  private LocalDateTime createdAt;
  private String contentType;
  private String sellerName;
  private String contentTitle;
  private int priceOptionLength;
  private String contentStatus;
  private String adminContentCheckingStatus;
}
