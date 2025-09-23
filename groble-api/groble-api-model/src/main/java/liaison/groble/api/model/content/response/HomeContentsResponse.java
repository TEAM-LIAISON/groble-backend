package liaison.groble.api.model.content.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class HomeContentsResponse {
  private List<ContentPreviewCardResponse> items;
}
