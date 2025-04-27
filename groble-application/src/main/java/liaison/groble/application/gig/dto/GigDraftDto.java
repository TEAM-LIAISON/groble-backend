package liaison.groble.application.gig.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GigDraftDto {
  private Long gigId;
  private String title;
  private String gigType;
  private Long categoryId;
  private String thumbnailUrl;
  private String status;
  private List<GigOptionDto> options;

  @Getter
  @Builder
  public static class GigOptionDto {
    private Long id;
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
