package liaison.groble.application.order.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.content.ContentReader;
import liaison.groble.application.guest.reader.GuestUserReader;
import liaison.groble.application.order.dto.CreateOrderRequestDTO;
import liaison.groble.application.order.dto.CreateOrderSuccessDTO;
import liaison.groble.application.order.dto.OrderSuccessDTO;
import liaison.groble.application.order.dto.ValidatedOrderOptionDTO;
import liaison.groble.application.payment.dto.billing.SubscriptionPaymentMetadata;
import liaison.groble.application.payment.dto.completion.FreePaymentCompletionResult;
import liaison.groble.application.payment.event.FreePaymentCompletedEvent;
import liaison.groble.application.payment.service.SubscriptionPaymentMetadataProvider;
import liaison.groble.application.purchase.service.PurchaseReader;
import liaison.groble.application.user.service.UserReader;
import liaison.groble.common.event.EventPublisher;
import liaison.groble.domain.content.entity.Content;
import liaison.groble.domain.content.entity.ContentOption;
import liaison.groble.domain.content.enums.ContentPaymentType;
import liaison.groble.domain.content.enums.SubscriptionSellStatus;
import liaison.groble.domain.coupon.entity.UserCoupon;
import liaison.groble.domain.coupon.repository.UserCouponRepository;
import liaison.groble.domain.guest.entity.GuestUser;
import liaison.groble.domain.order.entity.Order;
import liaison.groble.domain.order.entity.OrderItem;
import liaison.groble.domain.order.entity.Purchaser;
import liaison.groble.domain.order.repository.OrderRepository;
import liaison.groble.domain.order.vo.OrderOptionInfo;
import liaison.groble.domain.payment.entity.BillingKey;
import liaison.groble.domain.payment.entity.Payment;
import liaison.groble.domain.payment.repository.BillingKeyRepository;
import liaison.groble.domain.payment.repository.PaymentRepository;
import liaison.groble.domain.purchase.entity.Purchase;
import liaison.groble.domain.purchase.repository.PurchaseRepository;
import liaison.groble.domain.subscription.entity.Subscription;
import liaison.groble.domain.subscription.repository.SubscriptionRepository;
import liaison.groble.domain.user.entity.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 주문 관련 비즈니스 로직을 처리하는 서비스
 *
 * <p>주요 기능:
 *
 * <ul>
 *   <li>콘텐츠 구매를 위한 주문 생성
 *   <li>주문 옵션 검증 및 가격 계산
 *   <li>쿠폰 적용 및 할인 처리
 *   <li>무료 주문(0원 결제) 자동 처리
 *   <li>결제 완료 후 주문 정보 조회
 * </ul>
 *
 * @author [작성자]
 * @since [버전]
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

  // Reader
  private final UserReader userReader;
  private final GuestUserReader guestUserReader;
  private final ContentReader contentReader;
  private final PurchaseReader purchaseReader;

  // Repository
  private final OrderRepository orderRepository;
  private final UserCouponRepository userCouponRepository;
  private final PurchaseRepository purchaseRepository;
  private final PaymentRepository paymentRepository;
  private final SubscriptionRepository subscriptionRepository;
  private final BillingKeyRepository billingKeyRepository;

  // Event Publisher
  private final EventPublisher eventPublisher;
  private final SubscriptionPaymentMetadataProvider subscriptionPaymentMetadataProvider;

  @Transactional
  public CreateOrderSuccessDTO createOrderForUser(CreateOrderRequestDTO dto, Long userId) {
    log.info("회원 주문 생성 시작 - userId: {}, contentId: {}", userId, dto.getContentId());

    // 1. 사용자 조회
    final User user = userReader.getUserById(userId);

    // 2. 공통 로직으로 주문 생성
    return createOrderInternal(dto, user, null);
  }

  @Transactional
  public CreateOrderSuccessDTO createOrderForGuest(CreateOrderRequestDTO dto, Long guestUserId) {
    log.info("비회원 주문 생성 시작 - guestUserId: {}, contentId: {}", guestUserId, dto.getContentId());
    GuestUser guestUser = guestUserReader.getGuestUserById(guestUserId);

    // GuestUser 인증 상태 확인
    if (!guestUser.isVerified()) {
      throw new IllegalStateException("전화번호 인증이 완료되지 않은 게스트 사용자입니다.");
    }

    // 공통 로직으로 주문 생성 (쿠폰은 비회원이므로 처리하지 않음)
    return createOrderInternal(dto, null, guestUser);
  }

  /**
   * 주문 생성 공통 로직
   *
   * @param dto 주문 요청 정보
   * @param user 회원 (회원 주문시에만 제공)
   * @param guestUser 비회원 (비회원 주문시에만 제공)
   * @return 주문 생성 결과
   */
  private CreateOrderSuccessDTO createOrderInternal(
      CreateOrderRequestDTO dto, User user, GuestUser guestUser) {
    // 1. 콘텐츠 조회
    final Content content = contentReader.getContentById(dto.getContentId());

    if (content.getPaymentType() == ContentPaymentType.SUBSCRIPTION) {
      validateSubscriptionSellStatus(content);
      if (guestUser != null) {
        throw new IllegalArgumentException("정기결제 상품은 회원만 구매할 수 있습니다.");
      }
    }

    // 2. 주문 옵션 검증 및 변환
    final List<ValidatedOrderOptionDTO> validatedOptions =
        validateAndEnrichOptions(content, dto.getOptions());
    final List<OrderOptionInfo> orderOptions = convertToDomainOptions(validatedOptions);

    if (content.getPaymentType() == ContentPaymentType.SUBSCRIPTION) {
      if (validatedOptions.size() != 1) {
        throw new IllegalArgumentException("정기결제 상품은 하나의 옵션만 선택할 수 있습니다.");
      }

      ValidatedOrderOptionDTO option = validatedOptions.get(0);
      if (option.getQuantity() == null || option.getQuantity() != 1) {
        throw new IllegalArgumentException("정기결제 상품은 옵션 수량 1개만 구매할 수 있습니다.");
      }
    }

    // 3. 주문 객체 생성
    Order order = createOrderByUserType(user, guestUser, content, orderOptions);

    // 4. 회원인 경우에만 쿠폰 적용
    UserCoupon appliedCoupon = null;
    if (user != null && dto.getCouponCodes() != null && !dto.getCouponCodes().isEmpty()) {
      appliedCoupon = findAndValidateBestCoupon(order, user, dto.getCouponCodes());
      if (appliedCoupon != null) {
        order.applyCoupon(appliedCoupon);
      }
    }

    // 5. 무료 주문 여부 확인
    final boolean willBeFreePurchase = order.getFinalPrice().compareTo(BigDecimal.ZERO) == 0;

    // 6. 주문 저장 및 merchantUid 생성
    order = saveOrderWithMerchantUid(order);

    // 7. 무료 주문 처리
    if (willBeFreePurchase) {
      boolean success = processFreeOrderPurchase(order);
      if (!success) {
        throw new RuntimeException("무료 주문 처리 실패 - 트랜잭션 롤백");
      }
    }

    // 8. 주문 생성 로그 기록
    logOrderCreationByType(
        order, user, guestUser, dto.getContentId(), validatedOptions.size(), willBeFreePurchase);

    // 9. 응답 생성
    return buildCreateOrderDTOByType(order, user, guestUser, willBeFreePurchase);
  }

  private void validateSubscriptionSellStatus(Content content) {
    SubscriptionSellStatus sellStatus = content.getSubscriptionSellStatus();
    if (sellStatus == SubscriptionSellStatus.PAUSED) {
      throw new IllegalStateException("정기결제 신규 신청이 일시 중단된 콘텐츠입니다.");
    }
    if (sellStatus == SubscriptionSellStatus.TERMINATED) {
      throw new IllegalStateException("정기결제가 종료된 콘텐츠입니다.");
    }
  }

  /**
   * 주문 옵션 검증 및 상세 정보 추가
   *
   * <p>클라이언트가 요청한 옵션들이 실제 콘텐츠에 존재하는지 검증하고, 각 옵션의 가격 정보를 조회하여 검증된 옵션 정보를 생성합니다.
   *
   * @param content 구매하려는 콘텐츠
   * @param requestedOptions 클라이언트가 요청한 옵션 목록
   * @return 검증되고 가격 정보가 추가된 옵션 목록
   * @throws IllegalArgumentException 요청한 옵션이 콘텐츠에 존재하지 않는 경우
   */
  private List<ValidatedOrderOptionDTO> validateAndEnrichOptions(
      Content content, List<CreateOrderRequestDTO.OrderOptionDTO> requestedOptions) {

    if (requestedOptions == null || requestedOptions.isEmpty()) {
      throw new IllegalArgumentException("주문 옵션은 최소 1개 이상 선택해야 합니다.");
    }

    return requestedOptions.stream()
        .map(option -> validateAndEnrichSingleOption(content, option))
        .collect(Collectors.toList());
  }

  /**
   * 단일 주문 옵션 검증 및 상세 정보 추가
   *
   * @param content 콘텐츠 정보
   * @param requestedOption 요청된 옵션
   * @return 검증된 옵션 정보
   */
  private ValidatedOrderOptionDTO validateAndEnrichSingleOption(
      Content content, CreateOrderRequestDTO.OrderOptionDTO requestedOption) {

    // 콘텐츠에서 해당 옵션 찾기
    ContentOption matchedOption = findContentOption(content, requestedOption.getOptionId());

    // 수량 검증
    if (requestedOption.getQuantity() <= 0) {
      throw new IllegalArgumentException(
          "옵션 수량은 1개 이상이어야 합니다. optionId=" + requestedOption.getOptionId());
    }

    return ValidatedOrderOptionDTO.builder()
        .optionId(matchedOption.getId())
        .optionType(mapToDomainOptionType(requestedOption.getOptionType()))
        .price(matchedOption.getPrice())
        .quantity(requestedOption.getQuantity())
        .build();
  }

  /**
   * 콘텐츠에서 특정 옵션 찾기
   *
   * @param content 콘텐츠
   * @param optionId 찾으려는 옵션 ID
   * @return 매칭된 콘텐츠 옵션
   * @throws IllegalArgumentException 옵션을 찾을 수 없는 경우
   */
  private ContentOption findContentOption(Content content, Long optionId) {
    return content.getOptions().stream()
        .filter(opt -> opt.getId().equals(optionId))
        .findFirst()
        .orElseThrow(
            () ->
                new IllegalArgumentException(
                    String.format(
                        "콘텐츠(id=%d)에 요청한 옵션(id=%d)이 존재하지 않습니다.", content.getId(), optionId)));
  }

  /**
   * 클라이언트 옵션 타입을 도메인 옵션 타입으로 변환
   *
   * @param clientOptionType 클라이언트가 전송한 옵션 타입
   * @return 도메인 옵션 타입
   * @throws IllegalArgumentException 유효하지 않은 옵션 타입인 경우
   */
  private OrderItem.OptionType mapToDomainOptionType(
      CreateOrderRequestDTO.OrderOptionDTO.OptionType clientOptionType) {
    try {
      return OrderItem.OptionType.valueOf(clientOptionType.name());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("유효하지 않은 옵션 타입입니다. optionType=" + clientOptionType, e);
    }
  }

  /**
   * 주문 생성 및 저장
   *
   * <p>주문을 생성하고 DB에 저장한 후, 고유 주문 번호(merchantUid)를 생성하여 할당합니다. merchantUid는 주문 ID를 기반으로 생성되므로, 먼저
   * 주문을 저장하여 ID를 확보해야 합니다.
   *
   * @param user 주문 사용자
   * @param content 주문 콘텐츠
   * @param orderOptions 주문 옵션 정보
   * @param purchaser 구매자 정보
   * @return 저장된 주문
   */
  private Order createAndSaveOrder(
      User user, Content content, List<OrderOptionInfo> orderOptions, Purchaser purchaser) {

    // 주문 생성
    Order order = Order.createOrderWithMultipleOptions(user, content, orderOptions, purchaser);

    // 1차 저장 (ID 생성을 위함)
    order = orderRepository.save(order);

    // merchantUid 생성 및 할당
    order.setMerchantUid(Order.generateMerchantUid(order.getId()));

    // 2차 저장 (merchantUid 업데이트)
    return orderRepository.save(order);
  }

  /**
   * 최적의 쿠폰 찾기 및 검증 (저장 전 처리용)
   *
   * @param order 주문 (아직 저장되지 않은 상태)
   * @param user 사용자
   * @param couponCodes 쿠폰 코드 목록
   * @return 최적의 쿠폰 (없으면 null)
   */
  private UserCoupon findAndValidateBestCoupon(Order order, User user, List<String> couponCodes) {
    // 무료 상품은 쿠폰 적용 불가
    if (order.getOriginalPrice().compareTo(BigDecimal.ZERO) <= 0) {
      log.info("무료 상품(원가 0원)은 쿠폰 적용이 불가능합니다");
      return null;
    }

    return findBestCoupon(user, couponCodes, order.getOriginalPrice());
  }

  /**
   * 쿠폰 적용 처리
   *
   * <p>요청된 쿠폰들 중 가장 할인이 큰 쿠폰을 자동으로 선택하여 적용합니다. 무료 상품(원가 0원)에는 쿠폰을 적용할 수 없습니다.
   *
   * @param order 주문
   * @param user 사용자
   * @param couponCodes 적용하려는 쿠폰 코드 목록
   * @deprecated 트랜잭션 안전성을 위해 findAndValidateBestCoupon을 사용하세요
   */
  @Deprecated
  private void applyCouponIfRequested(Order order, User user, List<String> couponCodes) {
    // 쿠폰 코드가 없으면 처리하지 않음
    if (couponCodes == null || couponCodes.isEmpty()) {
      return;
    }

    // 무료 상품은 쿠폰 적용 불가
    if (order.getOriginalPrice().compareTo(BigDecimal.ZERO) <= 0) {
      log.info("무료 상품(원가 0원)은 쿠폰 적용이 불가능합니다 - orderId: {}", order.getId());
      return;
    }

    // 최적 쿠폰 찾기 및 적용
    UserCoupon bestCoupon = findBestCoupon(user, couponCodes, order.getOriginalPrice());
    if (bestCoupon != null) {
      order.applyCoupon(bestCoupon);
      log.info(
          "쿠폰 적용 완료 - orderId: {}, couponCode: {}, 할인금액: {}",
          order.getId(),
          bestCoupon.getCouponCode(),
          order.getCouponDiscountPrice());
    }
  }

  /**
   * 가장 유리한 쿠폰 찾기
   *
   * <p>여러 쿠폰 중 할인 금액이 가장 큰 쿠폰을 선택합니다. 각 쿠폰에 대해 다음을 검증합니다:
   *
   * <ul>
   *   <li>쿠폰 존재 여부
   *   <li>쿠폰 소유자 확인
   *   <li>쿠폰 사용 가능 상태
   *   <li>할인 금액 계산
   * </ul>
   *
   * @param user 사용자
   * @param couponCodes 쿠폰 코드 목록
   * @param orderPrice 주문 원가
   * @return 최적의 쿠폰 (없으면 null)
   */
  private UserCoupon findBestCoupon(User user, List<String> couponCodes, BigDecimal orderPrice) {
    UserCoupon bestCoupon = null;
    BigDecimal maxDiscount = BigDecimal.ZERO;

    for (String couponCode : couponCodes) {
      try {
        UserCoupon validatedCoupon = validateCoupon(user, couponCode);
        if (validatedCoupon == null) {
          continue;
        }

        // 할인 금액 계산
        BigDecimal discountPrice = calculateCouponDiscount(validatedCoupon, orderPrice);

        // 최대 할인 쿠폰 갱신
        if (discountPrice.compareTo(maxDiscount) > 0) {
          maxDiscount = discountPrice;
          bestCoupon = validatedCoupon;
        }

      } catch (Exception e) {
        log.error("쿠폰 검증 중 오류 발생 - couponCode: {}", couponCode, e);
      }
    }

    if (bestCoupon != null) {
      log.info("최적 쿠폰 선택 완료 - couponCode: {}, 할인금액: {}", bestCoupon.getCouponCode(), maxDiscount);
    }

    return bestCoupon;
  }

  /**
   * 쿠폰 유효성 검증
   *
   * @param user 사용자
   * @param couponCode 쿠폰 코드
   * @return 유효한 쿠폰 (유효하지 않으면 null)
   */
  private UserCoupon validateCoupon(User user, String couponCode) {
    // 쿠폰 조회
    UserCoupon coupon = userCouponRepository.findByCouponCode(couponCode).orElse(null);
    if (coupon == null) {
      log.warn("쿠폰을 찾을 수 없습니다: {}", couponCode);
      return null;
    }

    // 쿠폰 소유자 확인
    if (!coupon.getUser().getId().equals(user.getId())) {
      log.warn(
          "본인 소유가 아닌 쿠폰: {} (소유자: {}, 요청자: {})",
          couponCode,
          coupon.getUser().getId(),
          user.getId());
      return null;
    }

    // 쿠폰 사용 가능 여부 확인
    if (!coupon.isUsable()) {
      log.warn("사용할 수 없는 쿠폰: {} (이미 사용됨 또는 만료됨)", couponCode);
      return null;
    }

    return coupon;
  }

  /**
   * 쿠폰 할인 금액 계산
   *
   * @param coupon 쿠폰
   * @param orderPrice 주문 원가
   * @return 할인 금액
   */
  private BigDecimal calculateCouponDiscount(UserCoupon coupon, BigDecimal orderPrice) {
    return coupon.getCouponTemplate().calculateDiscountPrice(orderPrice);
  }

  /**
   * 무료 주문 처리
   *
   * <p>최종 금액이 0원인 주문은 별도의 결제 과정 없이 즉시 구매 완료 처리합니다. 데이터 일관성을 위해 Payment와 Purchase 엔티티를 모두 생성합니다.
   *
   * @param order 주문
   * @return 무료 주문 처리 여부
   */
  private boolean processIfFreeOrder(Order order) {
    if (order.getFinalPrice().compareTo(BigDecimal.ZERO) != 0) {
      return false;
    }

    boolean success = processFreeOrderPurchase(order);
    if (success) {
      log.info("무료 주문 즉시 구매 처리 완료 - orderId: {}, finalPrice: 0", order.getId());
    }
    return success;
  }

  /**
   * 무료 주문에 대한 구매 처리
   *
   * <p>무료 주문(0원 결제)을 즉시 완료 처리합니다. 정상적인 결제 프로세스와 동일한 데이터 구조를 유지하기 위해 Payment와 Purchase 엔티티를 모두 생성하고
   * 완료 상태로 설정합니다.
   *
   * @param order 무료 주문 (최종 금액이 0원)
   * @return 구매 처리 성공 여부
   */
  private boolean processFreeOrderPurchase(Order order) {
    try {
      // 1. 무료 Payment 생성 및 완료 처리
      Payment freePayment = createAndCompleteFreePayment(order);
      log.info("무료 결제 정보 생성 완료 - orderId: {}, paymentId: {}", order.getId(), freePayment.getId());
      // 2. Purchase 생성 및 완료 처리
      Purchase purchase = createAndCompletePurchase(order);
      log.info("무료 구매 정보 생성 완료 - orderId: {}, purchaseId: {}", order.getId(), purchase.getId());

      // 3. 주문 상태를 결제 완료로 변경
      order.completePayment();
      orderRepository.save(order);

      // 5-2. 승인 성공 시 결제 완료 처리 (회원/비회원 구분)
      FreePaymentCompletionResult.FreePaymentCompletionResultBuilder resultBuilder =
          FreePaymentCompletionResult.builder()
              .orderId(order.getId())
              .merchantUid(order.getMerchantUid())
              .paymentId(purchase.getPayment().getId())
              .purchaseId(purchase.getId())
              .contentId(purchase.getContent().getId())
              .sellerId(purchase.getContent().getUser().getId())
              .amount(purchase.getPayment().getPrice())
              .completedAt(purchase.getPurchasedAt())
              .sellerEmail(purchase.getContent().getUser().getEmail())
              .contentTitle(purchase.getContent().getTitle())
              .contentType(purchase.getContent().getContentType().name())
              .optionId(purchase.getSelectedOptionId())
              .selectedOptionName(purchase.getSelectedOptionName())
              .purchasedAt(purchase.getPurchasedAt());

      // 회원/비회원에 따른 정보 설정
      if (order.isMemberOrder()) {
        resultBuilder
            .userId(order.getUser().getId())
            .guestUserId(null)
            .nickname(order.getUser().getNickname());
      } else if (order.isGuestOrder()) {
        resultBuilder
            .userId(null) // 비회원은 userId가 없음
            .guestUserId(order.getGuestUser().getId())
            .guestUserName(order.getGuestUser().getUsername()); // GuestUser의 username 사용
      }

      FreePaymentCompletionResult freePaymentCompletionResult = resultBuilder.build();

      // 6. 결제 완료 이벤트 발행
      publishFreePaymentCompletedEvent(freePaymentCompletionResult);

      log.info(
          "무료 주문 구매 처리 성공 - orderId: {}, paymentId: {}, purchaseId: {}, userId: {}, contentId: {}",
          order.getId(),
          freePayment.getId(),
          purchase.getId(),
          order.isMemberOrder() && order.getUser() != null ? order.getUser().getId() : null,
          purchase.getContent().getId());

      return true;

    } catch (Exception e) {
      log.error("무료 주문 구매 처리 실패 - orderId: {}", order.getId(), e);
      handleFreeOrderFailure(order, e);
      return false;
    }
  }

  /**
   * 무료 결제 정보 생성 및 완료 처리
   *
   * @param order 주문
   * @return 생성된 결제 정보
   */
  private Payment createAndCompleteFreePayment(Order order) {
    Payment freePayment = Payment.createFreePayment(order);
    freePayment.completeFreePayment();
    Payment savedPayment = paymentRepository.save(freePayment);
    savedPayment.publishPaymentCreatedEvent();
    return savedPayment;
  }

  /**
   * 구매 정보 생성 및 완료 처리
   *
   * @param order 주문
   * @return 생성된 구매 정보
   */
  private Purchase createAndCompletePurchase(Order order) {
    Purchase purchase = Purchase.createFromOrder(order);
    return purchaseRepository.save(purchase);
  }

  /**
   * 무료 주문 처리 실패 시 처리
   *
   * @param order 주문
   * @param e 발생한 예외
   */
  private void handleFreeOrderFailure(Order order, Exception e) {
    try {
      order.failOrder("무료 구매 처리 실패: " + e.getMessage());
      orderRepository.save(order);
    } catch (Exception failException) {
      log.error("주문 실패 처리 중 추가 오류 발생 - orderId: {}", order.getId(), failException);
    }
  }

  /**
   * 결제 성공 후 주문 정보 조회 (회원용)
   *
   * <p>결제가 완료된 주문의 상세 정보를 조회합니다. 본인의 주문인지 권한을 검증하고, 결제 완료 상태인지 확인합니다.
   *
   * @param merchantUid 주문 고유 식별자
   * @param userId 요청한 사용자 ID (권한 검증용)
   * @return 주문 성공 정보
   * @throws IllegalArgumentException 주문을 찾을 수 없는 경우
   * @throws IllegalStateException 권한이 없거나 결제가 완료되지 않은 경우
   */
  @Transactional(readOnly = true)
  public OrderSuccessDTO getOrderSuccess(String merchantUid, Long userId) {
    // 1. 주문 조회
    Order order = findOrderByMerchantUid(merchantUid);

    // 2. 권한 검증
    validateOrderAccess(order, userId);

    // 3. 주문 상태 검증
    validateOrderPaidStatus(order);

    // 4. 구매 정보 조회
    Purchase purchase = purchaseReader.getPurchaseByOrderId(order.getId());

    // 5. 응답 생성
    return buildOrderSuccessResponse(order, purchase);
  }

  /**
   * 결제 성공 후 주문 정보 조회 (비회원용)
   *
   * <p>결제가 완료된 비회원 주문의 상세 정보를 조회합니다. 게스트 사용자의 주문인지 권한을 검증하고, 결제 완료 상태인지 확인합니다.
   *
   * @param merchantUid 주문 고유 식별자
   * @param guestUserId 요청한 게스트 사용자 ID (권한 검증용)
   * @return 주문 성공 정보
   * @throws IllegalArgumentException 주문을 찾을 수 없는 경우
   * @throws IllegalStateException 권한이 없거나 결제가 완료되지 않은 경우
   */
  @Transactional(readOnly = true)
  public OrderSuccessDTO getGuestOrderSuccess(String merchantUid, Long guestUserId) {
    // 1. 주문 조회
    Order order = findOrderByMerchantUid(merchantUid);

    // 2. 게스트 사용자 조회
    GuestUser guestUser = guestUserReader.getGuestUserById(guestUserId);

    // 3. 권한 검증 (비회원용)
    validateGuestOrderAccess(order, guestUser);

    // 4. 주문 상태 검증
    validateOrderPaidStatus(order);

    // 5. 구매 정보 조회
    Purchase purchase = purchaseReader.getPurchaseByOrderId(order.getId());

    // 6. 응답 생성
    return buildOrderSuccessResponse(order, purchase);
  }

  /**
   * merchantUid로 주문 조회
   *
   * @param merchantUid 주문 고유 식별자
   * @return 주문
   * @throws IllegalArgumentException 주문을 찾을 수 없는 경우
   */
  private Order findOrderByMerchantUid(String merchantUid) {
    return orderRepository
        .findByMerchantUid(merchantUid)
        .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + merchantUid));
  }

  /**
   * 주문 접근 권한 검증 (회원용)
   *
   * @param order 주문
   * @param userId 사용자 ID
   * @throws IllegalStateException 접근 권한이 없는 경우
   */
  private void validateOrderAccess(Order order, Long userId) {
    if (order.getUser() == null || !order.getUser().getId().equals(userId)) {
      throw new IllegalStateException("해당 주문에 대한 접근 권한이 없습니다. orderId=" + order.getId());
    }
  }

  /**
   * 주문 접근 권한 검증 (비회원용)
   *
   * @param order 주문
   * @param guestUser 게스트 사용자
   * @throws IllegalStateException 접근 권한이 없는 경우
   */
  private void validateGuestOrderAccess(Order order, GuestUser guestUser) {
    if (order.getGuestUser() == null || !order.getGuestUser().getId().equals(guestUser.getId())) {
      throw new IllegalStateException("해당 주문에 대한 접근 권한이 없습니다. orderId=" + order.getId());
    }
  }

  /**
   * 주문 결제 완료 상태 검증
   *
   * @param order 주문
   * @throws IllegalStateException 결제가 완료되지 않은 경우
   */
  private void validateOrderPaidStatus(Order order) {
    if (order.getStatus() != Order.OrderStatus.PAID) {
      throw new IllegalStateException(
          String.format(
              "결제가 완료되지 않은 주문입니다. orderId=%d, status=%s", order.getId(), order.getStatus()));
    }
  }

  /**
   * 주문 성공 응답 DTO 생성
   *
   * <p>결제 완료된 주문의 상세 정보를 포함한 응답을 생성합니다. 주문 정보, 구매 정보, 가격 정보, 쿠폰 정보 등을 포함합니다.
   *
   * @param order 주문
   * @param purchase 구매 정보
   * @return 주문 성공 응답
   */
  private OrderSuccessDTO buildOrderSuccessResponse(Order order, Purchase purchase) {
    // 첫 번째 주문 아이템 정보 (현재는 단일 콘텐츠 구매만 지원)
    OrderItem orderItem = order.getOrderItems().get(0);
    Content content = orderItem.getContent();

    log.info(
        "주문 성공 응답 생성 - orderId: {}, purchasedAt: {}", order.getId(), purchase.getPurchasedAt());

    LocalDate nextPaymentDate =
        subscriptionRepository
            .findByPurchaseId(purchase.getId())
            .map(Subscription::getNextBillingDate)
            .orElse(null);

    String cardName = null;
    String cardNumberLast4 = null;
    Payment payment = purchase.getPayment();
    if (payment != null) {
      String billingKeyValue = payment.getBillingKey();
      if (billingKeyValue != null && !billingKeyValue.isBlank()) {
        Optional<BillingKey> billingKeyOptional =
            billingKeyRepository.findByBillingKey(billingKeyValue);
        if (billingKeyOptional.isPresent()) {
          BillingKey billingKey = billingKeyOptional.get();
          cardName = billingKey.getCardName();
          cardNumberLast4 = extractCardLast4(billingKey.getCardNumberMasked());
        }
      }
    }

    return OrderSuccessDTO.builder()
        // 주문 기본 정보
        .merchantUid(order.getMerchantUid())
        .purchasedAt(purchase.getPurchasedAt())
        .contentId(content.getId())
        .contentTitle(content.getTitle())
        .paymentType(content.getPaymentType() != null ? content.getPaymentType().name() : null)
        .nextPaymentDate(nextPaymentDate)
        .cardName(cardName)
        .cardNumberLast4(cardNumberLast4)
        .sellerName(content.getUser().getNickname())
        .orderStatus(order.getStatus().name())

        // 콘텐츠 정보

        .contentThumbnailUrl(content.getThumbnailUrl())

        // 선택한 옵션 정보
        .selectedOptionId(orderItem.getOptionId())
        .selectedOptionType(
            orderItem.getOptionType() != null ? orderItem.getOptionType().name() : null)
        .selectedOptionName(purchase.getSelectedOptionName())
        // 가격 정보
        .originalPrice(order.getOriginalPrice())
        .discountPrice(order.getDiscountPrice())
        .finalPrice(order.getFinalPrice())

        // 구매 정보

        .isFreePurchase(order.getFinalPrice().compareTo(BigDecimal.ZERO) == 0)
        .build();
  }

  private String extractCardLast4(String masked) {
    if (masked == null || masked.isBlank()) {
      return null;
    }
    String digitsOnly = masked.replaceAll("\\D", "");
    if (digitsOnly.length() < 4) {
      return digitsOnly.isEmpty() ? null : digitsOnly;
    }
    return digitsOnly.substring(digitsOnly.length() - 4);
  }

  /**
   * 검증된 옵션 정보를 도메인 옵션 정보로 변환
   *
   * <p>검증이 완료된 옵션 정보를 도메인 엔티티에서 사용할 수 있는 형태로 변환합니다.
   *
   * @param options 검증된 옵션 목록
   * @return 도메인 옵션 정보 목록
   */
  private List<OrderOptionInfo> convertToDomainOptions(List<ValidatedOrderOptionDTO> options) {
    return options.stream().map(this::convertSingleOption).collect(Collectors.toList());
  }

  /**
   * 단일 옵션 변환
   *
   * @param option 검증된 옵션
   * @return 도메인 옵션 정보
   */
  private OrderOptionInfo convertSingleOption(ValidatedOrderOptionDTO option) {
    return OrderOptionInfo.builder()
        .optionId(option.getOptionId())
        .optionType(option.getOptionType())
        .price(option.getPrice())
        .quantity(option.getQuantity())
        .build();
  }

  /**
   * 사용자 타입에 따라 주문 생성
   *
   * @param user 회원 (null이면 비회원)
   * @param guestUser 비회원 (null이면 회원)
   * @param content 콘텐츠
   * @param orderOptions 주문 옵션들
   * @return 생성된 주문
   */
  private Order createOrderByUserType(
      User user, GuestUser guestUser, Content content, List<OrderOptionInfo> orderOptions) {
    if (user != null) {
      // 회원 주문
      final Purchaser purchaser = buildPurchaserFromUser(user);
      return Order.createOrderWithMultipleOptions(user, content, orderOptions, purchaser);
    } else if (guestUser != null) {
      // 비회원 주문 (GuestUser 연계)
      return Order.createGuestOrderWithMultipleOptions(guestUser, content, orderOptions);
    } else {
      throw new IllegalArgumentException("User 또는 GuestUser 중 하나는 반드시 제공되어야 합니다.");
    }
  }

  /**
   * 주문 저장 및 merchantUid 생성
   *
   * @param order 주문
   * @return 저장된 주문
   */
  private Order saveOrderWithMerchantUid(Order order) {
    // 1차 저장 (ID 생성)
    order = orderRepository.save(order);

    // merchantUid 생성 및 업데이트
    order.setMerchantUid(Order.generateMerchantUid(order.getId()));

    // 2차 저장 (merchantUid 업데이트)
    return orderRepository.save(order);
  }

  /**
   * 사용자 정보로부터 구매자 정보 생성
   *
   * <p>회원 주문의 경우에도 구매자 정보를 별도로 관리하여 주문 당시의 사용자 정보를 보존합니다.
   *
   * @param user 사용자 엔티티
   * @return 구매자 정보
   */
  private Purchaser buildPurchaserFromUser(User user) {
    return Purchaser.builder()
        .name(user.getNickname())
        .email(user.getEmail())
        .phone(user.getPhoneNumber())
        .build();
  }

  /**
   * 사용자 타입에 따른 주문 생성 로그 기록
   *
   * @param order 생성된 주문
   * @param user 회원 (null 가능)
   * @param guestUser 비회원 (null 가능)
   * @param contentId 콘텐츠 ID
   * @param optionCount 선택한 옵션 개수
   * @param isFree 무료 주문 여부
   */
  private void logOrderCreationByType(
      Order order,
      User user,
      GuestUser guestUser,
      Long contentId,
      int optionCount,
      boolean isFree) {
    String userInfo =
        (user != null) ? "userId: " + user.getId() : "guestUserId: " + guestUser.getId();
    String userType = (user != null) ? "회원" : "비회원";

    log.info(
        "{} 주문 생성 완료 - orderId: {}, merchantUid: {}, {}, contentId: {}, "
            + "옵션수: {}, 원가: {}원, 최종금액: {}원, 할인금액: {}원, 즉시구매: {}",
        userType,
        order.getId(),
        order.getMerchantUid(),
        userInfo,
        contentId,
        optionCount,
        order.getOriginalPrice(),
        order.getFinalPrice(),
        order.getDiscountPrice(),
        isFree);
  }

  /**
   * 사용자 타입에 따른 주문 응답 DTO 생성
   *
   * @param order 주문
   * @param user 회원 (null 가능)
   * @param guestUser 비회원 (null 가능)
   * @param isPurchasedContent 구매 완료 여부
   * @return 주문 생성 응답
   */
  private CreateOrderSuccessDTO buildCreateOrderDTOByType(
      Order order, User user, GuestUser guestUser, boolean isPurchasedContent) {
    // 첫 번째 주문 항목의 콘텐츠 제목
    String contentTitle = order.getOrderItems().get(0).getContent().getTitle();

    String email, phoneNumber;
    if (user != null) {
      // 회원 주문
      email = user.getEmail();
      phoneNumber = user.getPhoneNumber();
    } else {
      // 비회원 주문
      email = guestUser.getEmail();
      phoneNumber = guestUser.getPhoneNumber();
    }

    return CreateOrderSuccessDTO.builder()
        .merchantUid(order.getMerchantUid())
        .email(email)
        .phoneNumber(phoneNumber)
        .contentTitle(contentTitle)
        .totalPrice(order.getTotalPrice())
        .isPurchasedContent(isPurchasedContent)
        .paypleOptions(
            subscriptionPaymentMetadataProvider
                .buildForOrder(order)
                .map(this::toPaypleOptionsDTO)
                .orElse(null))
        .build();
  }

  private CreateOrderSuccessDTO.PaypleOptionsDTO toPaypleOptionsDTO(
      SubscriptionPaymentMetadata metadata) {
    return CreateOrderSuccessDTO.PaypleOptionsDTO.builder()
        .billingKeyAction(metadata.getBillingKeyAction().name())
        .payWork(metadata.getPayWork())
        .cardVer(metadata.getCardVer())
        .regularFlag(metadata.getRegularFlag())
        .defaultPayMethod(metadata.getDefaultPayMethod())
        .merchantUserKey(metadata.getMerchantUserKey())
        .billingKeyId(metadata.getBillingKeyId())
        .nextPaymentDate(metadata.getNextPaymentDate())
        .payYear(metadata.getPayYear())
        .payMonth(metadata.getPayMonth())
        .payDay(metadata.getPayDay())
        .build();
  }

  /** 결제 완료 이벤트 발행 */
  private void publishFreePaymentCompletedEvent(
      FreePaymentCompletionResult freePaymentCompletionResult) {
    log.info("=== 무료 결제 이벤트 발행 시작 === orderId: {}", freePaymentCompletionResult.getOrderId());

    try {
      FreePaymentCompletedEvent event =
          FreePaymentCompletedEvent.builder()
              .orderId(freePaymentCompletionResult.getOrderId())
              .merchantUid(freePaymentCompletionResult.getMerchantUid())
              .paymentId(freePaymentCompletionResult.getPaymentId())
              .purchaseId(freePaymentCompletionResult.getPurchaseId())
              .userId(freePaymentCompletionResult.getUserId())
              .guestUserId(freePaymentCompletionResult.getGuestUserId())
              .contentId(freePaymentCompletionResult.getContentId())
              .sellerId(freePaymentCompletionResult.getSellerId())
              .amount(freePaymentCompletionResult.getAmount())
              .completedAt(freePaymentCompletionResult.getCompletedAt())
              .sellerEmail(freePaymentCompletionResult.getSellerEmail())
              .contentTitle(freePaymentCompletionResult.getContentTitle())
              .nickname(freePaymentCompletionResult.getNickname())
              .guestUserName(freePaymentCompletionResult.getGuestUserName())
              .contentType(freePaymentCompletionResult.getContentType())
              .optionId(freePaymentCompletionResult.getOptionId())
              .selectedOptionName(freePaymentCompletionResult.getSelectedOptionName())
              .purchasedAt(freePaymentCompletionResult.getPurchasedAt())
              .build();

      log.info("무료 결제 이벤트 객체 생성 완료: orderId={}", event.getOrderId());

      eventPublisher.publish(event); // 이 부분이 있나요?

      log.info("=== 무료 결제 이벤트 발행 완료 === orderId: {}", freePaymentCompletionResult.getOrderId());

    } catch (Exception e) {
      log.error("무료 결제 이벤트 발행 중 예외 발생 - orderId: {}", freePaymentCompletionResult.getOrderId(), e);
      throw e; // 또는 log만 남기고 넘어갈지 결정
    }
  }
}
