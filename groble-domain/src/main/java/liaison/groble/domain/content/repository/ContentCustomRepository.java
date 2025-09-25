package liaison.groble.domain.content.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import liaison.groble.common.response.CursorResponse;
import liaison.groble.domain.content.dto.FlatAdminContentSummaryInfoDTO;
import liaison.groble.domain.content.dto.FlatContentOverviewDTO;
import liaison.groble.domain.content.dto.FlatContentPreviewDTO;
import liaison.groble.domain.content.dto.FlatDynamicContentDTO;
import liaison.groble.domain.content.enums.ContentStatus;
import liaison.groble.domain.content.enums.ContentType;

public interface ContentCustomRepository {

  Optional<FlatContentPreviewDTO> findRepresentativeContentByUser(Long userId);

  Optional<FlatContentPreviewDTO> findFlatContentById(Long contentId);

  List<FlatContentPreviewDTO> findFlatContentsByUserId(Long userId);

  List<FlatContentPreviewDTO> findHomeContents();

  List<FlatContentPreviewDTO> findAllMarketContentsByUserId(Long userId);

  CursorResponse<FlatContentPreviewDTO> findMyPurchasingContentsWithCursor(
      Long userId, Long lastContentId, int size, ContentStatus status, ContentType contentType);

  CursorResponse<FlatContentPreviewDTO> findMySellingContentsWithCursor(
      Long userId, Long lastContentId, int size, List<ContentStatus> statusList);

  CursorResponse<FlatContentPreviewDTO> findHomeContentsWithCursor(
      Long lastContentId, int size, ContentType contentType);

  /** 콘텐츠 타입만으로 페이징 조회 (카테고리가 없는 경우). */
  Page<FlatContentPreviewDTO> findContentsByType(ContentType contentType, Pageable pageable);

  /** 카테고리 + 콘텐츠 타입으로 페이징 조회. */
  Page<FlatContentPreviewDTO> findContentsByCategoriesAndType(
      List<String> categoryId, ContentType contentType, Pageable pageable);

  Page<FlatContentPreviewDTO> findAllMarketContentsByUserIdWithPaging(
      Long userId, Pageable pageable);

  int countMySellingContents(Long userId, List<ContentStatus> contentStatusList);

  int countMyPurchasingContents(Long userId, ContentStatus status, ContentType contentType);

  List<FlatDynamicContentDTO> findAllDynamicContents();

  Page<FlatAdminContentSummaryInfoDTO> findContentsByPageable(Pageable pageable);

  Page<FlatContentPreviewDTO> findMyContentsWithStatus(
      Pageable pageable, Long userId, List<ContentStatus> contentStatuses);

  Page<FlatContentOverviewDTO> findMyContentsBySellerId(Long sellerId, Pageable pageable);

  boolean existsSellingContentByUser(Long userId);

  boolean isAvailableForSale(Long contentId);

  long countByStatus(ContentStatus status);

  long countByStatusIn(List<ContentStatus> statuses);

  long countByContentTypeAndStatus(String contentType, ContentStatus status);

  long countByContentTypeAndStatusIn(String contentType, List<ContentStatus> statuses);
}
