package liaison.groble.api.model.content.response.dynamic;

import java.util.ArrayList;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DynamicContentListResponse {

  @Builder.Default
  @Schema(description = "동적 콘텐츠 목록", type = "array", requiredMode = Schema.RequiredMode.REQUIRED)
  private final List<DynamicContentResponse> dynamicContentResponses = new ArrayList<>();
}
