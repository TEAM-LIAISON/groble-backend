package liaison.groble.application.purchase;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.order.service.OrderReader;
import liaison.groble.application.purchase.dto.PurchaseContentCardDto;
import liaison.groble.application.purchase.dto.PurchasedContentDetailResponse;
import liaison.groble.application.purchase.service.PurchaseReader;
import liaison.groble.common.response.CursorResponse;
import liaison.groble.domain.content.enums.ContentType;
import liaison.groble.domain.order.entity.Order;
import liaison.groble.domain.purchase.dto.FlatPurchaseContentPreviewDTO;
import liaison.groble.domain.purchase.entity.Purchase;
import liaison.groble.domain.purchase.repository.PurchaseCustomRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PurchaseService {

  private final PurchaseCustomRepository purchaseCustomRepository;
  private final OrderReader orderReader;
  private final PurchaseReader purchaseReader;

  @Transactional(readOnly = true)
  public CursorResponse<PurchaseContentCardDto> getMyPurchasingContents(
      Long userId, String cursor, int size, String state, String type) {
    Long lastContentId = parseContentIdFromCursor(cursor);
    List<Order.OrderStatus> orderStatusList = parseOrderStatusList(state);
    ContentType contentType = parseContentType(type);

    CursorResponse<FlatPurchaseContentPreviewDTO> flatDtos =
        purchaseCustomRepository.findMyPurchasingContentsWithCursor(
            userId, lastContentId, size, orderStatusList, contentType);

    List<PurchaseContentCardDto> cardDtos =
        flatDtos.getItems().stream().map(this::convertFlatDtoToCardDto).toList();

    int totalCount =
        purchaseCustomRepository.countMyPurchasingContents(userId, orderStatusList, contentType);

    return CursorResponse.<PurchaseContentCardDto>builder()
        .items(cardDtos)
        .nextCursor(flatDtos.getNextCursor())
        .hasNext(flatDtos.isHasNext())
        .totalCount(totalCount)
        .meta(flatDtos.getMeta())
        .build();
  }

  @Transactional(readOnly = true)
  public PurchasedContentDetailResponse getMyPurchasedContent(Long userId, String merchantUid) {
    Order order = orderReader.getOrderByMerchantUid(merchantUid);

    if (!order.getUser().getId().equals(userId)) {
      throw new IllegalArgumentException("해당 주문은 사용자의 것이 아닙니다.");
    }

    Purchase purchase = purchaseReader.getPurchaseByOrderId(order.getId());
    return toPurchasedContentDetailResponse(purchase);
  }

  /** FlatPreviewContentDTO를 ContentCardDto로 변환합니다. */
  private PurchaseContentCardDto convertFlatDtoToCardDto(FlatPurchaseContentPreviewDTO flat) {
    return PurchaseContentCardDto.builder()
        .merchantUid(flat.getMerchantUid())
        .contentId(flat.getContentId())
        .contentType(flat.getContentType())
        .purchasedAt(flat.getPurchasedAt())
        .title(flat.getTitle())
        .thumbnailUrl(flat.getThumbnailUrl())
        .sellerName(flat.getSellerName())
        .originalPrice(flat.getOriginalPrice())
        .finalPrice(flat.getFinalPrice())
        .priceOptionLength(flat.getPriceOptionLength())
        .orderStatus(flat.getOrderStatus())
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
  private List<Order.OrderStatus> parseOrderStatusList(String state) {
    if (state == null || state.isBlank()) {
      return null;
    }

    try {
      return List.of(Order.OrderStatus.valueOf(state.toUpperCase()));
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

  /**
   * 구매한 상품 상세 응답 DTO 생성
   *
   * @param purchase 구매 정보
   */
  private PurchasedContentDetailResponse toPurchasedContentDetailResponse(Purchase purchase) {
    Order order = purchase.getOrder();
    var content = purchase.getContent();
    var seller = content.getUser();

    return PurchasedContentDetailResponse.builder()
        // 주문 정보
        .merchantUid(order.getMerchantUid())
        .purchasedAt(purchase.getPurchasedAt())

        // 콘텐츠 정보
        .contentId(content.getId())
        .contentTitle(content.getTitle())
        .sellerName(seller.getNickname())

        // 가격 정보
        .originalPrice(purchase.getOriginalPrice())
        .discountPrice(purchase.getDiscountPrice())
        .finalPrice(purchase.getFinalPrice())
        .isFreePurchase(purchase.getFinalPrice().signum() == 0)
        .build();
  }
}
