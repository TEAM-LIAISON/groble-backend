package liaison.groble.application.content.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ContentDto {
  private Long contentId;
  private String title;
  private String contentType;
  private Long categoryId;
  private String thumbnailUrl;
  private String status;
  private List<ContentOptionDto> options;

  @Getter
  @Builder
  public static class ContentOptionDto {
    private Long contentOptionId;
    private String name;
    private String description;
    private BigDecimal price;

    // 코칭 옵션 관련 필드
    private String coachingPeriod;
    private String documentProvision;
    private String coachingType;
    private String coachingTypeDescription;

    // 문서 옵션 관련 필드
    private String contentDeliveryMethod;
  }
}
