package liaison.groble.application.maker.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BusinessMakerInfoDTO {
  private final String businessName;
  private final String representativeName;
  private final String businessNumber;
  private final String businessAddress;
}
