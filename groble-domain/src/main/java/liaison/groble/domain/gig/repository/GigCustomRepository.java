package liaison.groble.domain.gig.repository;

import java.util.List;
import java.util.Optional;

import liaison.groble.common.response.CursorResponse;
import liaison.groble.domain.gig.dto.FlatPreviewGigDTO;
import liaison.groble.domain.gig.enums.GigStatus;

public interface GigCustomRepository {
  Optional<FlatPreviewGigDTO> findFlatGigById(Long gigId);

  List<FlatPreviewGigDTO> findFlatGigsByUserId(Long userId);

  // 커서 기반 페이지네이션으로 나의 코칭 상품 목록 조회
  CursorResponse<FlatPreviewGigDTO> findMyCoachingGigsWithCursor(
      Long userId, Long lastGigId, int size, List<Long> categoryIds, GigStatus status);

  // 내 코칭 상품 총 개수 조회
  int countMyCoachingGigs(Long userId, List<Long> categoryIds, GigStatus status);
}
