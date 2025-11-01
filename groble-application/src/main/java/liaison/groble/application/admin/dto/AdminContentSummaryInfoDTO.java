package liaison.groble.application.admin.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminContentSummaryInfoDTO {
  private Long contentId;
  private LocalDateTime createdAt;
  private String contentType;
  private String sellerName;
  private String contentTitle;
  private int priceOptionLength;
  private BigDecimal minPrice;
  private String contentStatus;
  private String adminContentCheckingStatus;
  private Boolean isSearchExposed;
  private List<AdminDocumentFileInfoDTO> documentFiles;
}
