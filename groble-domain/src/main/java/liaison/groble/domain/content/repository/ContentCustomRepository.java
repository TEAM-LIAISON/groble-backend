package liaison.groble.domain.content.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import liaison.groble.common.response.CursorResponse;
import liaison.groble.domain.content.dto.FlatContentPreviewDTO;
import liaison.groble.domain.content.enums.ContentStatus;
import liaison.groble.domain.content.enums.ContentType;

public interface ContentCustomRepository {
  Optional<FlatContentPreviewDTO> findFlatContentById(Long contentId);

  List<FlatContentPreviewDTO> findFlatContentsByUserId(Long userId);

  List<FlatContentPreviewDTO> findHomeContents(ContentType contentType);

  CursorResponse<FlatContentPreviewDTO> findMyPurchasingContentsWithCursor(
      Long userId, Long lastContentId, int size, ContentStatus status, ContentType contentType);

  CursorResponse<FlatContentPreviewDTO> findMySellingContentsWithCursor(
      Long userId,
      Long lastContentId,
      int size,
      List<ContentStatus> statusList,
      ContentType contentType);

  CursorResponse<FlatContentPreviewDTO> findHomeContentsWithCursor(
      Long lastContentId, int size, ContentType contentType);

  Page<FlatContentPreviewDTO> findContentsByCategoryAndType(
      Long categoryId, ContentType contentType, Pageable pageable);

  int countMySellingContents(
      Long userId, List<ContentStatus> contentStatusList, ContentType contentType);

  int countMyPurchasingContents(Long userId, ContentStatus status, ContentType contentType);
}
