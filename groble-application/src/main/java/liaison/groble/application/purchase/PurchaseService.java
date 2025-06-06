package liaison.groble.application.purchase;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.purchase.dto.PurchaseContentCardDto;
import liaison.groble.common.response.CursorResponse;
import liaison.groble.domain.content.enums.ContentType;
import liaison.groble.domain.purchase.dto.FlatPurchaseContentPreviewDTO;
import liaison.groble.domain.purchase.enums.PurchaseStatus;
import liaison.groble.domain.purchase.repository.PurchaseCustomRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PurchaseService {

  private final PurchaseCustomRepository purchaseCustomRepository;

  @Transactional(readOnly = true)
  public CursorResponse<PurchaseContentCardDto> getMyPurchasingContents(
      Long userId, String cursor, int size, String state, String type) {
    Long lastContentId = parseContentIdFromCursor(cursor);
    List<PurchaseStatus> contentStatusList = parsePurchaseStatusList(state);
    ContentType contentType = parseContentType(type);

    CursorResponse<FlatPurchaseContentPreviewDTO> flatDtos =
        purchaseCustomRepository.findMyPurchasingContentsWithCursor(
            userId, lastContentId, size, contentStatusList, contentType);

    List<PurchaseContentCardDto> cardDtos =
        flatDtos.getItems().stream().map(this::convertFlatDtoToCardDto).toList();

    int totalCount =
        purchaseCustomRepository.countMyPurchasingContents(userId, contentStatusList, contentType);

    // 7. 응답 구성
    return CursorResponse.<PurchaseContentCardDto>builder()
        .items(cardDtos)
        .nextCursor(flatDtos.getNextCursor())
        .hasNext(flatDtos.isHasNext())
        .totalCount(totalCount)
        .meta(flatDtos.getMeta())
        .build();
  }

  /** FlatPreviewContentDTO를 ContentCardDto로 변환합니다. */
  private PurchaseContentCardDto convertFlatDtoToCardDto(FlatPurchaseContentPreviewDTO flat) {
    return PurchaseContentCardDto.builder()
        .contentId(flat.getContentId())
        .purchasedAt(flat.getCreatedAt())
        .title(flat.getTitle())
        .thumbnailUrl(flat.getThumbnailUrl())
        .sellerName(flat.getSellerName())
        .originalPrice(flat.getOriginalPrice())
        .priceOptionLength(flat.getPriceOptionLength())
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
  private List<PurchaseStatus> parsePurchaseStatusList(String state) {
    if (state == null || state.isBlank()) {
      return null;
    }

    try {
      return List.of(PurchaseStatus.valueOf(state.toUpperCase()));
    } catch (IllegalArgumentException e) {
      log.warn("유효하지 않은 구매 상태: {}", state);
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
      log.warn("유효하지 않은 콘텐츠 유형: {}", type);
      return null;
    }
  }
}
