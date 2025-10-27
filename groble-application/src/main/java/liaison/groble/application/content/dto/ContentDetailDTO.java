package liaison.groble.application.content.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ContentDetailDTO {
  private Long contentId;
  private String status;
  private String thumbnailUrl;
  private String contentType;
  private String paymentType;
  private String categoryId;
  private String title;
  private Boolean isSearchExposed;
  private String sellerProfileImageUrl;
  private String sellerName;
  private BigDecimal lowestPrice;
  private List<ContentOptionDTO> options;
  private String contentIntroduction;
  private String serviceTarget;
  private String serviceProcess;
  private String makerIntro;
}
