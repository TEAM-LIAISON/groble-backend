package liaison.groble.api.model.content.response;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DynamicContentListResponse {
  @Builder.Default
  private final List<DynamicContentResponse> dynamicContentResponses = new ArrayList<>();
}
