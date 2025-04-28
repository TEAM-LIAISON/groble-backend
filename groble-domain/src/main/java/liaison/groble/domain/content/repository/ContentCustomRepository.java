package liaison.groble.domain.content.repository;

import java.util.List;
import java.util.Optional;

import liaison.groble.common.response.CursorResponse;
import liaison.groble.domain.content.dto.FlatPreviewContentDTO;
import liaison.groble.domain.content.enums.ContentStatus;

public interface ContentCustomRepository {
  Optional<FlatPreviewContentDTO> findFlatContentById(Long contentId);

  List<FlatPreviewContentDTO> findFlatContentsByUserId(Long userId);

  // 커서 기반 페이지네이션으로 나의 코칭 상품 목록 조회
  CursorResponse<FlatPreviewContentDTO> findMyCoachingContentsWithCursor(
      Long userId, Long lastContentId, int size, List<Long> categoryIds, ContentStatus status);

  // 내 코칭 상품 총 개수 조회
  int countMyCoachingContents(Long userId, List<Long> categoryIds, ContentStatus status);
}
