package liaison.groble.application.purchase;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.content.dto.ContentCardDto;
import liaison.groble.common.response.CursorResponse;
import liaison.groble.domain.content.dto.FlatContentPreviewDTO;
import liaison.groble.domain.content.enums.ContentStatus;
import liaison.groble.domain.content.enums.ContentType;
import liaison.groble.domain.content.repository.ContentCustomRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PurchaseService {

  private final ContentCustomRepository contentCustomRepository;

  @Transactional(readOnly = true)
  public CursorResponse<ContentCardDto> getMyPurchasingContents(
      Long userId, String cursor, int size, String state, String type) {
    Long lastContentId = parseContentIdFromCursor(cursor);
    ContentStatus contentStatus = parseContentStatus(state);
    ContentType contentType = parseContentType(type);

    CursorResponse<FlatContentPreviewDTO> flatDtos =
        contentCustomRepository.findMyPurchasingContentsWithCursor(
            userId, lastContentId, size, contentStatus, contentType);

    List<ContentCardDto> cardDtos =
        flatDtos.getItems().stream().map(this::convertFlatDtoToCardDto).toList();

    int totalCount =
        contentCustomRepository.countMyPurchasingContents(userId, contentStatus, contentType);

    // 7. 응답 구성
    return CursorResponse.<ContentCardDto>builder()
        .items(cardDtos)
        .nextCursor(flatDtos.getNextCursor())
        .hasNext(flatDtos.isHasNext())
        .totalCount(totalCount)
        .meta(flatDtos.getMeta())
        .build();
  }

  /** FlatPreviewContentDTO를 ContentCardDto로 변환합니다. */
  private ContentCardDto convertFlatDtoToCardDto(FlatContentPreviewDTO flat) {
    return ContentCardDto.builder()
        .contentId(flat.getContentId())
        .createdAt(flat.getCreatedAt())
        .title(flat.getTitle())
        .thumbnailUrl(flat.getThumbnailUrl())
        .sellerName(flat.getSellerName())
        .status(flat.getStatus())
        .build();
  }

  /** 커서에서 Content ID를 파싱합니다. */
  private Long parseContentIdFromCursor(String cursor) {
    if (cursor == null || cursor.isBlank()) {
      return null;
    }

    try {
      return Long.parseLong(cursor);
    } catch (NumberFormatException e) {
      log.warn("유효하지 않은 커서 형식: {}", cursor);
      return null;
    }
  }

  /** 문자열에서 ContentStatus를 파싱합니다. */
  private ContentStatus parseContentStatus(String state) {
    if (state == null || state.isBlank()) {
      return null;
    }

    try {
      return ContentStatus.valueOf(state.toUpperCase());
    } catch (IllegalArgumentException e) {
      log.warn("유효하지 않은 상품 상태: {}", state);
      return null;
    }
  }

  private ContentType parseContentType(String type) {
    if (type == null || type.isBlank()) {
      return null;
    }

    try {
      return ContentType.valueOf(type.toUpperCase());
    } catch (IllegalArgumentException e) {
      log.warn("유효하지 않은 컨텐츠 유형: {}", type);
      return null;
    }
  }
}
