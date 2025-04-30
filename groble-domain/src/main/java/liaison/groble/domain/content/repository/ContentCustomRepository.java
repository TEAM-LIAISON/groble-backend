package liaison.groble.domain.content.repository;

import java.util.List;
import java.util.Optional;

import liaison.groble.common.response.CursorResponse;
import liaison.groble.domain.content.dto.FlatPreviewContentDTO;
import liaison.groble.domain.content.enums.ContentStatus;
import liaison.groble.domain.content.enums.ContentType;

public interface ContentCustomRepository {
  Optional<FlatPreviewContentDTO> findFlatContentById(Long contentId);

  List<FlatPreviewContentDTO> findFlatContentsByUserId(Long userId);

  CursorResponse<FlatPreviewContentDTO> findMyPurchasingContentsWithCursor(
      Long userId, Long lastContentId, int size, ContentStatus status, ContentType contentType);

  CursorResponse<FlatPreviewContentDTO> findMySellingContentsWithCursor(
      Long userId, Long lastContentId, int size, ContentStatus status, ContentType contentType);

  CursorResponse<FlatPreviewContentDTO> findHomeContentsWithCursor(
      Long lastContentId, int size, ContentType contentType);

  int countMySellingContents(Long userId, ContentStatus status, ContentType contentType);

  int countMyPurchasingContents(Long userId, ContentStatus status, ContentType contentType);
}
