package liaison.groble.application.content.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ContentDetailDto {
  private Long contentId;
  private String status;
  private List<String> contentsImageUrls;
  private String contentType;
  private Long categoryId;
  private String title;
  private String sellerProfileImageUrl;
  private String sellerName;
  private BigDecimal lowestPrice;
  private List<ContentOptionDto> options;
  private String contentIntroduction;
  private List<String> contentDetailImageUrls;
  private String serviceTarget;
  private String serviceProcess;
  private String makerIntro;
}
