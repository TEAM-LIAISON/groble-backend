package liaison.groble.api.model.content.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
@Schema(description = "문서 옵션 정보 응답")
public class DocumentOptionResponse extends BaseOptionResponse {
  @Schema(description = "옵션 유형", example = "DOCUMENT_OPTION")
  private final String optionType = "DOCUMENT_OPTION";

  @Schema(description = "컨텐츠 제공 방식", example = "IMMEDIATE_DOWNLOAD")
  private String contentDeliveryMethod;
}
