package liaison.groble.application.purchase.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.guest.reader.GuestUserReader;
import liaison.groble.application.market.dto.ContactInfoDTO;
import liaison.groble.application.order.service.OrderReader;
import liaison.groble.application.payment.service.BillingKeyService;
import liaison.groble.application.purchase.dto.PurchaseContentCardDTO;
import liaison.groble.application.purchase.dto.PurchasedContentDetailDTO;
import liaison.groble.application.sell.SellerContactReader;
import liaison.groble.common.exception.ContactNotFoundException;
import liaison.groble.common.response.PageResponse;
import liaison.groble.domain.content.enums.ContentPaymentType;
import liaison.groble.domain.order.entity.Order;
import liaison.groble.domain.order.entity.OrderItem;
import liaison.groble.domain.purchase.dto.FlatPurchaseContentDetailDTO;
import liaison.groble.domain.purchase.dto.FlatPurchaseContentPreviewDTO;
import liaison.groble.domain.user.entity.SellerContact;
import liaison.groble.domain.user.entity.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PurchaseService {

  private static final ZoneId DEFAULT_TIME_ZONE = ZoneId.of("Asia/Seoul");
  private static final long CANCEL_AVAILABLE_DAYS = 7L;

  private final OrderReader orderReader;
  private final PurchaseReader purchaseReader;
  private final SellerContactReader sellerContactReader;
  private final GuestUserReader guestUserReader;
  private final BillingKeyService billingKeyService;

  // 내가 구매한 콘텐츠 목록 조회 (회원용)
  @Transactional(readOnly = true)
  public PageResponse<PurchaseContentCardDTO> getMyPurchasedContents(
      Long userId, String state, Pageable pageable) {
    List<Order.OrderStatus> orderStatuses = parseOrderStatuses(state);

    Page<FlatPurchaseContentPreviewDTO> page =
        purchaseReader.findMyPurchasedContents(userId, orderStatuses, pageable);

    List<PurchaseContentCardDTO> items =
        page.getContent().stream().map(this::convertFlatDTOToCardDTO).toList();

    PageResponse.MetaData meta =
        PageResponse.MetaData.builder()
            .sortBy(pageable.getSort().iterator().next().getProperty())
            .sortDirection(pageable.getSort().iterator().next().getDirection().name())
            .build();

    return PageResponse.from(page, items, meta);
  }

  // 내가 구매한 콘텐츠 목록 조회 (비회원용)
  @Transactional(readOnly = true)
  public PageResponse<PurchaseContentCardDTO> getMyPurchasedContentsForGuest(
      Long guestUserId, String state, Pageable pageable) {
    List<Order.OrderStatus> orderStatuses = parseOrderStatuses(state);

    String guestPhoneNumber = guestUserReader.getGuestUserById(guestUserId).getPhoneNumber();

    Page<FlatPurchaseContentPreviewDTO> page =
        purchaseReader.findMyPurchasedContentsForGuest(guestPhoneNumber, orderStatuses, pageable);

    List<PurchaseContentCardDTO> items =
        page.getContent().stream().map(this::convertFlatDTOToCardDTO).toList();

    PageResponse.MetaData meta =
        PageResponse.MetaData.builder()
            .sortBy(pageable.getSort().iterator().next().getProperty())
            .sortDirection(pageable.getSort().iterator().next().getDirection().name())
            .build();

    return PageResponse.from(page, items, meta);
  }

  // 내가 구매한 콘텐츠 상세 조회
  @Transactional(readOnly = true)
  public PurchasedContentDetailDTO getMyPurchasedContent(Long userId, String merchantUid) {

    FlatPurchaseContentDetailDTO flatPurchaseContentDetailDTO =
        purchaseReader.getPurchaseContentDetail(userId, merchantUid);

    boolean canResumeSubscription = false;
    if (ContentPaymentType.SUBSCRIPTION
        .name()
        .equals(flatPurchaseContentDetailDTO.getPaymentType())) {
      canResumeSubscription = billingKeyService.findActiveBillingKey(userId).isPresent();
    }

    return toPurchasedContentDetailDTO(flatPurchaseContentDetailDTO, canResumeSubscription);
  }

  private PurchaseContentCardDTO convertFlatDTOToCardDTO(FlatPurchaseContentPreviewDTO flat) {
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
        .paymentType(flat.getPaymentType())
        .subscriptionRound(flat.getSubscriptionRound())
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

  // 비회원 판매자 연락처 정보 조회
  @Transactional(readOnly = true)
  public ContactInfoDTO getContactInfoForGuest(Long guestUserId, String merchantUid) {
    Order order = orderReader.getOrderByMerchantUidAndGuestUserId(merchantUid, guestUserId);

    List<OrderItem> items = order.getOrderItems();
    if (items.isEmpty()) {
      throw new IllegalStateException("주문에 아이템이 없습니다.");
    }

    User user = items.get(0).getContent().getUser();
    return getContactInfo(user);
  }

  // 비회원 구매 콘텐츠 상세 조회
  @Transactional(readOnly = true)
  public PurchasedContentDetailDTO getMyPurchasedContentForGuest(
      Long guestUserId, String merchantUid) {
    FlatPurchaseContentDetailDTO flatPurchaseContentDetailDTO =
        purchaseReader.getPurchaseContentDetailForGuest(merchantUid);

    return toPurchasedContentDetailDTO(flatPurchaseContentDetailDTO, false);
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

  private PurchasedContentDetailDTO toPurchasedContentDetailDTO(
      FlatPurchaseContentDetailDTO flat, boolean canResumeSubscription) {
    return PurchasedContentDetailDTO.builder()
        .orderStatus(flat.getOrderStatus())
        .merchantUid(flat.getMerchantUid())
        .purchasedAt(flat.getPurchasedAt())
        .cancelRequestedAt(flat.getCancelRequestedAt())
        .cancelledAt(flat.getCancelledAt())
        .contentId(flat.getContentId())
        .sellerName(flat.getSellerName())
        .contentTitle(flat.getContentTitle())
        .selectedOptionName(flat.getSelectedOptionName())
        .selectedOptionQuantity(flat.getSelectedOptionQuantity())
        .selectedOptionType(flat.getSelectedOptionType())
        .documentOptionActionUrl(flat.getDocumentOptionActionUrl())
        .isFreePurchase(flat.getIsFreePurchase())
        .originalPrice(flat.getOriginalPrice())
        .discountPrice(flat.getDiscountPrice())
        .finalPrice(flat.getFinalPrice())
        .payType(flat.getPayType())
        .payCardName(flat.getPayCardName())
        .payCardNum(flat.getPayCardNum())
        .thumbnailUrl(flat.getThumbnailUrl())
        .isRefundable(flat.getIsRefundable())
        .isCancelable(isCancelable(flat))
        .cancelReason(flat.getCancelReason())
        .paymentType(flat.getPaymentType())
        .nextPaymentDate(resolveNextPaymentDate(flat))
        .subscriptionRound(flat.getSubscriptionRound())
        .subscriptionStatus(flat.getSubscriptionStatus())
        .isSubscriptionTerminated(flat.getIsSubscriptionTerminated())
        .billingFailureReason(flat.getBillingFailureReason())
        .canResumeSubscription(canResumeSubscription)
        .build();
  }

  private boolean isCancelable(FlatPurchaseContentDetailDTO flat) {
    if (flat == null) {
      return false;
    }

    if (!Order.OrderStatus.PAID.name().equals(flat.getOrderStatus())) {
      return false;
    }

    LocalDateTime purchasedAt = flat.getPurchasedAt();
    if (purchasedAt == null) {
      return false;
    }

    LocalDateTime now = LocalDateTime.now(DEFAULT_TIME_ZONE);
    return now.isBefore(purchasedAt.plusDays(CANCEL_AVAILABLE_DAYS));
  }

  private LocalDate resolveNextPaymentDate(FlatPurchaseContentDetailDTO flat) {
    if (flat == null) {
      return null;
    }

    boolean isSubscription =
        flat.getPaymentType() != null
            && ContentPaymentType.SUBSCRIPTION.name().equals(flat.getPaymentType());

    if (!isSubscription) {
      return null;
    }

    if (flat.getNextPaymentDate() != null) {
      return flat.getNextPaymentDate();
    }

    LocalDateTime purchasedAt = flat.getPurchasedAt();
    if (purchasedAt == null) {
      return null;
    }

    // 기본적으로 한 달 뒤 결제 예정일을 계산해 반환 (DB 값이 사라진 경우 대비)
    return purchasedAt.toLocalDate().plusMonths(1);
  }
}
