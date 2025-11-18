package liaison.groble.domain.content.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlatAdminDocumentFileDTO {
  private Long contentId;
  private Long optionId;
  private String optionName;
  private String documentOriginalFileName;
  private String documentFileUrl;
}
