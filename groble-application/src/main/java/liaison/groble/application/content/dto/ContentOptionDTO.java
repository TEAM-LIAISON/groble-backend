package liaison.groble.application.content.dto;

import java.math.BigDecimal;

import liaison.groble.domain.content.enums.ContentType;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ContentOptionDTO {
  private Long contentOptionId;
  private String name;
  private String description;
  private BigDecimal price;

  // enum OptionType { COACHING, DOCUMENT }
  private ContentType contentType;

  private Boolean hasSalesHistory;

  // document 전용
  private String documentOriginalFileName;
  private String documentFileUrl;
  private String documentLinkUrl;
}
