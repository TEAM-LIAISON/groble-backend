package liaison.groble.api.model.content.response;

import com.fasterxml.jackson.annotation.JsonTypeName;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@JsonTypeName("DOCUMENT_OPTION")
@Getter
@SuperBuilder
@NoArgsConstructor
@Schema(name = "DocumentOptionResponse", description = "문서 옵션 정보 응답")
public class DocumentOptionResponse extends BaseOptionResponse {
  @Override
  public String getOptionType() {
    return "DOCUMENT_OPTION";
  }

  @Schema(
      description = "컨텐츠 제공 방식",
      example = "IMMEDIATE_DOWNLOAD - [즉시 다운로드], FUTURE_UPLOAD - [추후 업로드]")
  private String contentDeliveryMethod;

  @Schema(description = "문서 파일 URL", example = "https://example.com/document.pdf")
  private String documentFileUrl;

  @Schema(description = "자료 링크 URL", example = "https://example.com/document-link")
  private String documentLinkUrl;
}
