package liaison.groble.application.content.dto;

import java.math.BigDecimal;

import liaison.groble.domain.content.enums.ContentType;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ContentOptionDto {
  private Long contentOptionId;
  private String name;
  private String description;
  private BigDecimal price;

  // enum OptionType { COACHING, DOCUMENT }
  private ContentType contentType;

  // coaching 전용
  private String coachingPeriod;
  private String documentProvision;
  private String coachingType;
  private String coachingTypeDescription;

  // document 전용
  private String contentDeliveryMethod;
}
