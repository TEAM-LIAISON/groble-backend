package liaison.groble.api.model.admin.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDocumentFileResponse {

  @Schema(description = "옵션 ID", example = "101")
  private Long optionId;

  @Schema(description = "옵션 이름", example = "PDF 다운로드 버전")
  private String optionName;

  @Schema(description = "파일 원본 이름", example = "샘플자료.pdf")
  private String documentOriginalFileName;

  @Schema(description = "파일 다운로드 URL", example = "https://cdn.groble.im/contents/document/file.pdf")
  private String documentFileUrl;
}
