package liaison.groble.application.market.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MarketEditDTO {
  private String marketName;
  private String profileImageUrl;
  private String marketLinkUrl;
  private ContactInfoDTO contactInfo;
  private Long representativeContentId;
}
