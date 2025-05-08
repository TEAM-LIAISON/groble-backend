package liaison.groble.persistence.scrap;

import liaison.groble.common.response.CursorResponse;
import liaison.groble.domain.content.dto.FlatScrapContentPreviewDTO;
import liaison.groble.domain.content.enums.ContentType;

public interface ContentScrapCustomRepository {

  CursorResponse<FlatScrapContentPreviewDTO> getMyScrapContentsWithCursor(
      Long userId, Long lastContentId, int size, ContentType contentType);

  int countMyScrapContents(Long userId, ContentType contentType);
}
