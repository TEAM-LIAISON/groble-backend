package liaison.groble.api.model.content.request.draft;

import jakarta.validation.constraints.Pattern;

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
public class DocumentOptionDraftRequest extends BaseOptionDraftRequest {
  @Pattern(regexp = "^(IMMEDIATE_DOWNLOAD|FUTURE_UPLOAD)$", message = "유효한 콘텐츠 제공 방식이 아닙니다")
  @Schema(
      description = "콘텐츠 제공 방식 [IMMEDIATE_DOWNLOAD - 즉시 업로드], [FUTURE_UPLOAD - 추후 업로드]",
      example = "IMMEDIATE_DOWNLOAD")
  private String contentDeliveryMethod;

  @Schema(description = "자료 파일 URL", example = "https://example.com/document.pdf")
  private String documentFileUrl;

  @Schema(description = "자료 링크 URL", example = "https://example.com/document-link")
  private String documentLinkUrl;
}
