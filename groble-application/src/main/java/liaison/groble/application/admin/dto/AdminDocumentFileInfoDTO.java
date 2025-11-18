package liaison.groble.application.admin.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminDocumentFileInfoDTO {
  private Long optionId;
  private String optionName;
  private String documentOriginalFileName;
  private String documentFileUrl;
}
