package liaison.groble.api.model.content.request.register;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/** 문서 옵션 요청 클래스 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentOptionRegisterRequest extends BaseOptionRegisterRequest {
  @Schema(description = "자료 파일 URL", example = "https://example.com/document.pdf")
  private String documentFileUrl;

  @Schema(description = "자료 링크 URL", example = "https://example.com/document-link")
  private String documentLinkUrl;
}
