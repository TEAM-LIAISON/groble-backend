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
import liaison.groble.domain.subscription.entity.Subscription;
import liaison.groble.domain.subscription.enums.SubscriptionStatus;
import liaison.groble.domain.subscription.repository.SubscriptionRepository;
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
  private final SubscriptionRepository subscriptionRepository;

  // 내가 구매한 콘텐츠 목록 조회 (회원용)
  @Transactional(readOnly = true)
  public PageResponse<PurchaseContentCardDTO> getMyPurchasedContents(
      Long userId, String state, Pageable pageable) {
    List<Order.OrderStatus> orderStatuses = parseOrderStatuses(state);

    Page<FlatPurchaseContentPreviewDTO> page =
        purchaseReader.findMyPurchasedContents(userId, orderStatuses, pageable);

    List<PurchaseContentCardDTO> items =
        page.getContent().stream().map(flat -> convertFlatDTOToCardDTO(flat, userId)).toList();
    items = filterItemsByState(items, state);

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
        page.getContent().stream().map(flat -> convertFlatDTOToCardDTO(flat, null)).toList();
    items = filterItemsByState(items, state);

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
    populateSubscriptionFields(flatPurchaseContentDetailDTO, userId);

    boolean canResumeSubscription = false;
    if (ContentPaymentType.SUBSCRIPTION
        .name()
        .equals(flatPurchaseContentDetailDTO.getPaymentType())) {
      boolean hasActiveBillingKey = billingKeyService.findActiveBillingKey(userId).isPresent();
      boolean terminated =
          Boolean.TRUE.equals(flatPurchaseContentDetailDTO.getIsSubscriptionTerminated());
      String subscriptionStatus = flatPurchaseContentDetailDTO.getSubscriptionStatus();
      boolean cancellableStatus =
          subscriptionStatus != null
              && SubscriptionStatus.CANCELLED.name().equalsIgnoreCase(subscriptionStatus);
      canResumeSubscription = hasActiveBillingKey && !terminated && cancellableStatus;
    }

    return toPurchasedContentDetailDTO(flatPurchaseContentDetailDTO, canResumeSubscription);
  }

  private PurchaseContentCardDTO convertFlatDTOToCardDTO(
      FlatPurchaseContentPreviewDTO flat, Long fallbackUserId) {
    populateSubscriptionFields(flat, fallbackUserId);

    String displayStatus =
        resolveDisplayOrderStatus(
            flat.getOrderStatus(),
            flat.getPaymentType(),
            flat.getSubscriptionStatus(),
            flat.getIsSubscriptionTerminated());

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
        .orderStatus(displayStatus)
        .paymentType(flat.getPaymentType())
        .subscriptionRound(flat.getSubscriptionRound())
        .subscriptionStatus(flat.getSubscriptionStatus())
        .isSubscriptionTerminated(flat.getIsSubscriptionTerminated())
        .billingFailureReason(flat.getBillingFailureReason())
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
    populateSubscriptionFields(flatPurchaseContentDetailDTO, null);

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
    String displayStatus =
        resolveDisplayOrderStatus(
            flat.getOrderStatus(),
            flat.getPaymentType(),
            flat.getSubscriptionStatus(),
            flat.getIsSubscriptionTerminated());

    return PurchasedContentDetailDTO.builder()
        .orderStatus(displayStatus)
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
        .isRefundable(resolveIsRefundable(flat))
        .isCancelable(resolveIsCancelable(flat))
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

  private boolean resolveIsCancelable(FlatPurchaseContentDetailDTO flat) {
    if (flat == null) {
      return false;
    }

    if (isSubscriptionOrder(flat)) {
      return isActiveSubscription(
          flat.getPaymentType(), flat.getSubscriptionStatus(), flat.getIsSubscriptionTerminated());
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

  private Boolean resolveIsRefundable(FlatPurchaseContentDetailDTO flat) {
    if (flat == null) {
      return false;
    }

    if (isSubscriptionOrder(flat)) {
      return isActiveSubscription(
          flat.getPaymentType(), flat.getSubscriptionStatus(), flat.getIsSubscriptionTerminated());
    }

    return flat.getIsRefundable();
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

  private void populateSubscriptionFields(FlatPurchaseContentDetailDTO flat, Long fallbackUserId) {
    if (!isSubscriptionOrder(flat)) {
      return;
    }

    boolean needsStatus = flat.getSubscriptionStatus() == null;
    boolean needsTermination = flat.getIsSubscriptionTerminated() == null;
    boolean needsFailureReason = flat.getBillingFailureReason() == null;

    if (!needsStatus && !needsTermination && !needsFailureReason) {
      return;
    }

    Long resolvedUserId = flat.getUserId() != null ? flat.getUserId() : fallbackUserId;
    if (resolvedUserId == null) {
      return;
    }

    resolveSubscription(flat.getMerchantUid(), flat.getContentId(), resolvedUserId)
        .ifPresent(subscription -> applySubscriptionDetails(flat, subscription));
  }

  private void populateSubscriptionFields(FlatPurchaseContentPreviewDTO flat, Long fallbackUserId) {
    if (!isSubscriptionOrder(flat)) {
      return;
    }

    boolean needsStatus = flat.getSubscriptionStatus() == null;
    boolean needsTermination = flat.getIsSubscriptionTerminated() == null;
    boolean needsFailureReason = flat.getBillingFailureReason() == null;

    if (!needsStatus && !needsTermination && !needsFailureReason) {
      return;
    }

    Long resolvedUserId = flat.getUserId() != null ? flat.getUserId() : fallbackUserId;
    if (resolvedUserId == null) {
      return;
    }

    resolveSubscription(flat.getMerchantUid(), flat.getContentId(), resolvedUserId)
        .ifPresent(subscription -> applySubscriptionDetails(flat, subscription));
  }

  private void applySubscriptionDetails(
      FlatPurchaseContentDetailDTO flat, Subscription subscription) {

    if (flat.getSubscriptionStatus() == null) {
      flat.setSubscriptionStatus(subscription.getStatus().name());
    }

    if (flat.getIsSubscriptionTerminated() == null) {
      boolean terminated = subscription.getGracePeriodEndsAt() != null;
      flat.setSubscriptionTerminated(terminated);
    }

    if (flat.getBillingFailureReason() == null) {
      flat.setBillingFailureReason(subscription.getLastBillingFailureReason());
    }
  }

  private void applySubscriptionDetails(
      FlatPurchaseContentPreviewDTO flat, Subscription subscription) {

    if (flat.getSubscriptionStatus() == null) {
      flat.setSubscriptionStatus(subscription.getStatus().name());
    }

    if (flat.getIsSubscriptionTerminated() == null) {
      boolean terminated = subscription.getGracePeriodEndsAt() != null;
      flat.setSubscriptionTerminated(terminated);
    }

    if (flat.getBillingFailureReason() == null) {
      flat.setBillingFailureReason(subscription.getLastBillingFailureReason());
    }
  }

  private java.util.Optional<Subscription> resolveSubscription(
      String merchantUid, Long contentId, Long userId) {
    if (merchantUid == null || userId == null) {
      return java.util.Optional.empty();
    }

    return subscriptionRepository
        .findByMerchantUidAndUserId(merchantUid, userId)
        .or(() -> subscriptionRepository.findByContentIdAndUserId(contentId, userId));
  }

  private String resolveDisplayOrderStatus(
      String originalOrderStatus,
      String paymentType,
      String subscriptionStatus,
      Boolean isSubscriptionTerminated) {

    if (isActiveSubscription(paymentType, subscriptionStatus, isSubscriptionTerminated)) {
      return Order.OrderStatus.PAID.name();
    }

    return originalOrderStatus;
  }

  private boolean isActiveSubscription(
      String paymentType, String subscriptionStatus, Boolean isSubscriptionTerminated) {

    if (paymentType == null
        || !ContentPaymentType.SUBSCRIPTION.name().equalsIgnoreCase(paymentType)
        || subscriptionStatus == null) {
      return false;
    }

    boolean terminated = isSubscriptionTerminated != null && isSubscriptionTerminated;

    if ("ACTIVE".equalsIgnoreCase(subscriptionStatus)) {
      return true;
    }

    if ("PAST_DUE".equalsIgnoreCase(subscriptionStatus)) {
      return !terminated;
    }

    if ("CANCELLED".equalsIgnoreCase(subscriptionStatus)) {
      return !terminated;
    }

    return false;
  }

  private boolean isSubscriptionOrder(FlatPurchaseContentDetailDTO flat) {
    return flat != null
        && flat.getPaymentType() != null
        && ContentPaymentType.SUBSCRIPTION.name().equals(flat.getPaymentType())
        && flat.getMerchantUid() != null;
  }

  private boolean isSubscriptionOrder(FlatPurchaseContentPreviewDTO flat) {
    return flat != null
        && flat.getPaymentType() != null
        && ContentPaymentType.SUBSCRIPTION.name().equals(flat.getPaymentType())
        && flat.getMerchantUid() != null;
  }

  private List<PurchaseContentCardDTO> filterItemsByState(
      List<PurchaseContentCardDTO> items, String state) {
    if (state == null || state.isBlank()) {
      return items;
    }

    String normalized = state.trim().toUpperCase();
    if ("PAID".equals(normalized)) {
      return items.stream()
          .filter(item -> Order.OrderStatus.PAID.name().equalsIgnoreCase(item.getOrderStatus()))
          .toList();
    }

    if ("CANCEL".equals(normalized)) {
      return items.stream().filter(this::isCancelDisplayStatus).toList();
    }

    return items;
  }

  private boolean isCancelDisplayStatus(PurchaseContentCardDTO item) {
    String status = item.getOrderStatus();
    if (status == null) {
      return false;
    }
    return Order.OrderStatus.CANCEL_REQUEST.name().equalsIgnoreCase(status)
        || Order.OrderStatus.CANCELLED.name().equalsIgnoreCase(status);
  }
}
