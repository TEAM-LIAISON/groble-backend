package liaison.groble.application.purchase.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.market.dto.ContactInfoDTO;
import liaison.groble.application.order.service.OrderReader;
import liaison.groble.application.purchase.dto.PurchaseContentCardDTO;
import liaison.groble.application.purchase.dto.PurchasedContentDetailDTO;
import liaison.groble.application.sell.SellerContactReader;
import liaison.groble.common.exception.ContactNotFoundException;
import liaison.groble.common.response.PageResponse;
import liaison.groble.domain.order.entity.Order;
import liaison.groble.domain.order.entity.OrderItem;
import liaison.groble.domain.purchase.dto.FlatPurchaseContentPreviewDTO;
import liaison.groble.domain.purchase.entity.Purchase;
import liaison.groble.domain.user.entity.SellerContact;
import liaison.groble.domain.user.entity.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PurchaseService {

  private final OrderReader orderReader;
  private final PurchaseReader purchaseReader;
  private final SellerContactReader sellerContactReader;

  @Transactional(readOnly = true)
  public PageResponse<PurchaseContentCardDTO> getMyPurchasedContents(
      Long userId, String state, Pageable pageable) {
    List<Order.OrderStatus> orderStatuses = parseOrderStatuses(state);

    Page<FlatPurchaseContentPreviewDTO> page =
        purchaseReader.findMyPurchasedContents(userId, orderStatuses, pageable);

    List<PurchaseContentCardDTO> items =
        page.getContent().stream().map(this::convertFlatDtoToCardDto).toList();

    PageResponse.MetaData meta =
        PageResponse.MetaData.builder()
            .sortBy(pageable.getSort().iterator().next().getProperty())
            .sortDirection(pageable.getSort().iterator().next().getDirection().name())
            .build();

    return PageResponse.from(page, items, meta);
  }

  @Transactional(readOnly = true)
  public PurchasedContentDetailDTO getMyPurchasedContent(Long userId, String merchantUid) {
    // 주문 정보 조회
    Order order = orderReader.getOrderByMerchantUidAndUserId(merchantUid, userId);

    // 구매 정보 조회
    Purchase purchase = purchaseReader.getPurchaseByOrderId(order.getId());

    return toPurchasedContentDetailDTO(purchase);
  }

  /** FlatPreviewContentDTO를 ContentCardDto로 변환합니다. */
  private PurchaseContentCardDTO convertFlatDtoToCardDto(FlatPurchaseContentPreviewDTO flat) {
    return PurchaseContentCardDTO.builder()
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
        .build();
  }

  /**
   * 구매한 상품 상세 응답 DTO 생성
   *
   * @param purchase 구매 정보
   */
  private PurchasedContentDetailDTO toPurchasedContentDetailDTO(Purchase purchase) {
    var order = purchase.getOrder();
    var content = purchase.getContent();
    var seller = content.getUser();
    boolean isRefundable = false;
    if (order.getStatus().equals(Order.OrderStatus.PAID)) {
      isRefundable = true;
    }

    return PurchasedContentDetailDTO.builder()
        .orderStatus(order.getStatus().name())
        // 주문 정보
        .merchantUid(order.getMerchantUid())
        .purchasedAt(purchase.getPurchasedAt())
        // 콘텐츠 정보
        .contentId(content.getId())
        .contentTitle(content.getTitle())
        .sellerName(seller.getNickname())
        .originalPrice(purchase.getOriginalPrice())
        .discountPrice(purchase.getDiscountPrice())
        .finalPrice(purchase.getFinalPrice())
        .isFreePurchase(purchase.getFinalPrice().signum() == 0)
        .thumbnailUrl(content.getThumbnailUrl())
        .isRefundable(isRefundable)
        .cancelReason(getCancelReasonSafely(purchase))
        .build();
  }

  @Transactional(readOnly = true)
  public ContactInfoDTO getContactInfo(Long userId, String merchantUid) {
    Order order = orderReader.getOrderByMerchantUidAndUserId(merchantUid, userId);

    List<OrderItem> items = order.getOrderItems();
    if (items.isEmpty()) {
      throw new IllegalStateException("주문에 아이템이 없습니다.");
    }

    User user = items.get(0).getContent().getUser();
    return getContactInfo(user);
  }

  private ContactInfoDTO getContactInfo(User user) {
    try {
      List<SellerContact> contacts = sellerContactReader.getContactsByUser(user);
      return ContactInfoDTO.from(contacts);
    } catch (ContactNotFoundException e) {
      log.warn("판매자 연락처 정보 없음: userId={}", user.getId());
      return ContactInfoDTO.builder().build();
    }
  }

  private List<Order.OrderStatus> parseOrderStatuses(String state) {
    if (state == null || state.isBlank()) {
      return null;
    }

    if ("CANCEL".equalsIgnoreCase(state)) {
      return List.of(Order.OrderStatus.CANCEL_REQUEST, Order.OrderStatus.CANCELLED);
    }

    return List.of(parseOrderStatus(state));
  }

  /** 문자열에서 OrderStatus 파싱합니다. */
  private Order.OrderStatus parseOrderStatus(String state) {
    if (state == null || state.isBlank()) {
      return null;
    }

    try {
      return Order.OrderStatus.valueOf(state.toUpperCase());
    } catch (IllegalArgumentException e) {
      log.warn("유효하지 않은 구매 상태: {}", state);
      return null;
    }
  }

  private String getCancelReasonSafely(Purchase purchase) {
    return Optional.ofNullable(purchase.getCancelReason()).map(Enum::name).orElse(null);
  }
}
