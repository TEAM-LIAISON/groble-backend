package liaison.groble.application.order.strategy;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;

import liaison.groble.application.content.ContentReader;
import liaison.groble.application.guest.reader.GuestUserReader;
import liaison.groble.application.order.dto.CreateOrderRequestDTO;
import liaison.groble.application.order.dto.CreateOrderSuccessDTO;
import liaison.groble.application.order.dto.OrderSuccessDTO;
import liaison.groble.application.order.dto.ValidatedOrderOptionDTO;
import liaison.groble.application.payment.dto.completion.FreePaymentCompletionResult;
import liaison.groble.application.payment.event.PaymentCompletedEvent;
import liaison.groble.application.purchase.service.PurchaseReader;
import liaison.groble.common.context.UserContext;
import liaison.groble.common.event.EventPublisher;
import liaison.groble.domain.content.entity.Content;
import liaison.groble.domain.content.entity.ContentOption;
import liaison.groble.domain.coupon.entity.UserCoupon;
import liaison.groble.domain.coupon.repository.UserCouponRepository;
import liaison.groble.domain.guest.repository.GuestUserRepository;
import liaison.groble.domain.order.entity.Order;
import liaison.groble.domain.order.entity.OrderItem;
import liaison.groble.domain.order.repository.OrderRepository;
import liaison.groble.domain.order.vo.OrderOptionInfo;
import liaison.groble.domain.payment.entity.Payment;
import liaison.groble.domain.payment.repository.PaymentRepository;
import liaison.groble.domain.purchase.entity.Purchase;
import liaison.groble.domain.purchase.repository.PurchaseRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * 주문 처리 전략의 기본 클래스
 *
 * <p>Template Method Pattern을 사용하여 공통 로직을 정의하고, 사용자 타입별 차이점만 서브클래스에서 구현합니다.
 */
@Slf4j
public abstract class BaseOrderProcessor implements OrderProcessorStrategy {

  // Readers
  protected ContentReader contentReader;
  protected PurchaseReader purchaseReader;
  protected GuestUserReader guestUserReader;

  // Repositories
  protected OrderRepository orderRepository;
  protected UserCouponRepository userCouponRepository;
  protected PurchaseRepository purchaseRepository;
  protected PaymentRepository paymentRepository;
  protected GuestUserRepository guestUserRepository;

  // Event Publisher
  protected EventPublisher eventPublisher;

  protected BaseOrderProcessor(
      ContentReader contentReader,
      PurchaseReader purchaseReader,
      GuestUserReader guestUserReader,
      OrderRepository orderRepository,
      UserCouponRepository userCouponRepository,
      PurchaseRepository purchaseRepository,
      PaymentRepository paymentRepository,
      GuestUserRepository guestUserRepository,
      EventPublisher eventPublisher) {
    log.info("=== BaseOrderProcessor 생성자 호출 시작 ===");
    log.info("contentReader: {}", contentReader);
    log.info("purchaseReader: {}", purchaseReader);
    log.info("guestUserReader: {}", guestUserReader);

    this.contentReader = contentReader;
    this.purchaseReader = purchaseReader;
    this.guestUserReader = guestUserReader;
    this.orderRepository = orderRepository;
    this.userCouponRepository = userCouponRepository;
    this.purchaseRepository = purchaseRepository;
    this.paymentRepository = paymentRepository;
    this.guestUserRepository = guestUserRepository;
    this.eventPublisher = eventPublisher;

    log.info("=== BaseOrderProcessor 생성자 완료 - this.contentReader: {} ===", this.contentReader);
  }

  @Override
  public final CreateOrderSuccessDTO createOrder(
      UserContext userContext,
      CreateOrderRequestDTO createOrderRequestDTO,
      HttpServletRequest httpRequest) {
    log.info(
        "{} 주문 생성 시작 - userId: {}, contentId: {}",
        getUserTypeString(userContext),
        userContext.getId(),
        createOrderRequestDTO.getContentId());

    log.info("=== createOrder 시점의 contentReader 상태 확인 ===");
    log.info("this.contentReader: {}", this.contentReader);
    log.info("contentReader == null: {}", this.contentReader == null);

    // 1. 콘텐츠 조회
    final Content content = contentReader.getContentById(createOrderRequestDTO.getContentId());

    // 2. 주문 옵션 검증 및 변환
    final List<ValidatedOrderOptionDTO> validatedOptions =
        validateAndEnrichOptions(content, createOrderRequestDTO.getOptions());
    final List<OrderOptionInfo> orderOptions = convertToDomainOptions(validatedOptions);

    // 3. 주문 객체 생성 (사용자 타입별 구현)
    Order order = createOrderByUserType(userContext, content, orderOptions);

    // 4. 쿠폰 적용 (회원인 경우에만)
    UserCoupon appliedCoupon = null;
    if (userContext.isMember()
        && createOrderRequestDTO.getCouponCodes() != null
        && !createOrderRequestDTO.getCouponCodes().isEmpty()) {
      appliedCoupon =
          findAndValidateBestCoupon(order, userContext, createOrderRequestDTO.getCouponCodes());
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
      if (!success) throw new RuntimeException("무료 주문 처리 실패 - 트랜잭션 롤백");

      // Guest 전용 처리는 서브클래스에서 처리
      handlePostFreeOrderProcessing(order, userContext, createOrderRequestDTO);
    }

    // 9. 주문 생성 로그 기록
    logOrderCreation(
        order,
        userContext,
        createOrderRequestDTO.getContentId(),
        validatedOptions.size(),
        willBeFreePurchase);

    // 10. 응답 생성 (사용자 타입별 구현)
    return buildCreateOrderResponse(order, userContext, willBeFreePurchase);
  }

  @Override
  public final OrderSuccessDTO getOrderSuccess(UserContext userContext, String merchantUid) {
    // 1. 주문 조회
    Order order = findOrderByMerchantUid(merchantUid);

    // 2. 권한 검증 (사용자 타입별 구현)
    validateOrderAccess(order, userContext);

    // 3. 주문 상태 검증
    validateOrderPaidStatus(order);

    // 4. 구매 정보 조회
    Purchase purchase = purchaseReader.getPurchaseByOrderId(order.getId());

    // 5. 응답 생성
    return buildOrderSuccessResponse(order, purchase);
  }

  // ===== Template Methods (서브클래스에서 구현) =====

  /** 사용자 타입에 따라 주문 생성 */
  protected abstract Order createOrderByUserType(
      UserContext userContext, Content content, List<OrderOptionInfo> orderOptions);

  /** 주문 접근 권한 검증 */
  protected abstract void validateOrderAccess(Order order, UserContext userContext);

  /** 주문 생성 응답 DTO 생성 */
  protected abstract CreateOrderSuccessDTO buildCreateOrderResponse(
      Order order, UserContext userContext, boolean isPurchasedContent);

  /** 쿠폰 찾기 및 검증 (회원만 구현) */
  protected abstract UserCoupon findAndValidateBestCoupon(
      Order order, UserContext userContext, List<String> couponCodes);

  /** 무료 주문 후 처리 (Guest 전용 로직 등) */
  protected abstract void handlePostFreeOrderProcessing(
      Order order, UserContext userContext, CreateOrderRequestDTO createOrderRequestDTO);

  // ===== 공통 메서드들 =====

  /** 주문 옵션 검증 및 상세 정보 추가 */
  protected List<ValidatedOrderOptionDTO> validateAndEnrichOptions(
      Content content, List<CreateOrderRequestDTO.OrderOptionDTO> requestedOptions) {

    if (requestedOptions == null || requestedOptions.isEmpty()) {
      throw new IllegalArgumentException("주문 옵션은 최소 1개 이상 선택해야 합니다.");
    }

    return requestedOptions.stream()
        .map(option -> validateAndEnrichSingleOption(content, option))
        .collect(Collectors.toList());
  }

  /** 단일 주문 옵션 검증 및 상세 정보 추가 */
  protected ValidatedOrderOptionDTO validateAndEnrichSingleOption(
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

  /** 콘텐츠에서 특정 옵션 찾기 */
  protected ContentOption findContentOption(Content content, Long optionId) {
    return content.getOptions().stream()
        .filter(opt -> opt.getId().equals(optionId))
        .findFirst()
        .orElseThrow(
            () ->
                new IllegalArgumentException(
                    String.format(
                        "콘텐츠(id=%d)에 요청한 옵션(id=%d)이 존재하지 않습니다.", content.getId(), optionId)));
  }

  /** 클라이언트 옵션 타입을 도메인 옵션 타입으로 변환 */
  protected OrderItem.OptionType mapToDomainOptionType(
      CreateOrderRequestDTO.OrderOptionDTO.OptionType clientOptionType) {
    try {
      return OrderItem.OptionType.valueOf(clientOptionType.name());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("유효하지 않은 옵션 타입입니다. optionType=" + clientOptionType, e);
    }
  }

  /** 검증된 옵션 정보를 도메인 옵션 정보로 변환 */
  protected List<OrderOptionInfo> convertToDomainOptions(List<ValidatedOrderOptionDTO> options) {
    return options.stream().map(this::convertSingleOption).collect(Collectors.toList());
  }

  /** 단일 옵션 변환 */
  protected OrderOptionInfo convertSingleOption(ValidatedOrderOptionDTO option) {
    return OrderOptionInfo.builder()
        .optionId(option.getOptionId())
        .optionType(option.getOptionType())
        .price(option.getPrice())
        .quantity(option.getQuantity())
        .build();
  }

  /** 주문 저장 및 merchantUid 생성 */
  protected Order saveOrderWithMerchantUid(Order order) {
    // 1차 저장 (ID 생성)
    order = orderRepository.save(order);

    // merchantUid 생성 및 업데이트
    order.setMerchantUid(Order.generateMerchantUid(order.getId()));

    // 2차 저장 (merchantUid 업데이트)
    return orderRepository.save(order);
  }

  /** 무료 주문에 대한 구매 처리 */
  protected boolean processFreeOrderPurchase(Order order) {
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

      // 4. 결제 완료 결과 생성
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
        resultBuilder.userId(order.getUser().getId()).nickname(order.getUser().getNickname());
      } else if (order.isGuestOrder()) {
        resultBuilder
            .userId(null) // 비회원은 userId가 없음
            .nickname(order.getGuestUser().getUsername()); // GuestUser의 username 사용
      }

      FreePaymentCompletionResult freePaymentCompletionResult = resultBuilder.build();

      // 5. 결제 완료 이벤트 발행
      publishFreePaymentCompletedEvent(freePaymentCompletionResult);

      log.info(
          "무료 주문 구매 처리 성공 - orderId: {}, paymentId: {}, purchaseId: {}",
          order.getId(),
          freePayment.getId(),
          purchase.getId());

      return true;

    } catch (Exception e) {
      log.error("무료 주문 구매 처리 실패 - orderId: {}", order.getId(), e);
      handleFreeOrderFailure(order, e);
      return false;
    }
  }

  /** 무료 결제 정보 생성 및 완료 처리 */
  protected Payment createAndCompleteFreePayment(Order order) {
    Payment freePayment = Payment.createFreePayment(order);
    Payment savedPayment = paymentRepository.save(freePayment);
    savedPayment.completeFreePayment();
    savedPayment.publishPaymentCreatedEvent();
    return savedPayment;
  }

  /** 구매 정보 생성 및 완료 처리 */
  protected Purchase createAndCompletePurchase(Order order) {
    Purchase purchase = Purchase.createFromOrder(order);
    return purchaseRepository.save(purchase);
  }

  /** 무료 주문 처리 실패 시 처리 */
  protected void handleFreeOrderFailure(Order order, Exception e) {
    try {
      order.failOrder("무료 구매 처리 실패: " + e.getMessage());
      orderRepository.save(order);
    } catch (Exception failException) {
      log.error("주문 실패 처리 중 추가 오류 발생 - orderId: {}", order.getId(), failException);
    }
  }

  /** merchantUid로 주문 조회 */
  protected Order findOrderByMerchantUid(String merchantUid) {
    return orderRepository
        .findByMerchantUid(merchantUid)
        .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + merchantUid));
  }

  /** 주문 결제 완료 상태 검증 */
  protected void validateOrderPaidStatus(Order order) {
    if (order.getStatus() != Order.OrderStatus.PAID) {
      throw new IllegalStateException(
          String.format(
              "결제가 완료되지 않은 주문입니다. orderId=%d, status=%s", order.getId(), order.getStatus()));
    }
  }

  /** 주문 성공 응답 DTO 생성 */
  protected OrderSuccessDTO buildOrderSuccessResponse(Order order, Purchase purchase) {
    // 첫 번째 주문 아이템 정보 (현재는 단일 콘텐츠 구매만 지원)
    OrderItem orderItem = order.getOrderItems().get(0);
    Content content = orderItem.getContent();

    log.info(
        "주문 성공 응답 생성 - orderId: {}, purchasedAt: {}", order.getId(), purchase.getPurchasedAt());

    return OrderSuccessDTO.builder()
        // 주문 기본 정보
        .merchantUid(order.getMerchantUid())
        .purchasedAt(purchase.getPurchasedAt())
        .contentId(content.getId())
        .contentTitle(content.getTitle())
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

  /** 주문 생성 로그 기록 */
  protected void logOrderCreation(
      Order order, UserContext userContext, Long contentId, int optionCount, boolean isFree) {
    String userTypeInfo = getUserTypeString(userContext);

    log.info(
        "{} 주문 생성 완료 - orderId: {}, merchantUid: {}, userId: {}, contentId: {}, "
            + "옵션수: {}, 원가: {}원, 최종금액: {}원, 할인금액: {}원, 즉시구매: {}",
        userTypeInfo,
        order.getId(),
        order.getMerchantUid(),
        userContext.getId(),
        contentId,
        optionCount,
        order.getOriginalPrice(),
        order.getFinalPrice(),
        order.getDiscountPrice(),
        isFree);
  }

  /** 결제 완료 이벤트 발행 */
  protected void publishFreePaymentCompletedEvent(
      FreePaymentCompletionResult freePaymentCompletionResult) {
    log.info("=== 무료 결제 이벤트 발행 시작 === orderId: {}", freePaymentCompletionResult.getOrderId());

    try {
      PaymentCompletedEvent event =
          PaymentCompletedEvent.builder()
              .orderId(freePaymentCompletionResult.getOrderId())
              .merchantUid(freePaymentCompletionResult.getMerchantUid())
              .paymentId(freePaymentCompletionResult.getPaymentId())
              .purchaseId(freePaymentCompletionResult.getPurchaseId())
              .userId(freePaymentCompletionResult.getUserId())
              .contentId(freePaymentCompletionResult.getContentId())
              .sellerId(freePaymentCompletionResult.getSellerId())
              .amount(freePaymentCompletionResult.getAmount())
              .completedAt(freePaymentCompletionResult.getCompletedAt())
              .sellerEmail(freePaymentCompletionResult.getSellerEmail())
              .contentTitle(freePaymentCompletionResult.getContentTitle())
              .nickname(freePaymentCompletionResult.getNickname())
              .contentType(freePaymentCompletionResult.getContentType())
              .optionId(freePaymentCompletionResult.getOptionId())
              .selectedOptionName(freePaymentCompletionResult.getSelectedOptionName())
              .purchasedAt(freePaymentCompletionResult.getPurchasedAt())
              .build();

      log.info("무료 결제 이벤트 객체 생성 완료: orderId={}", event.getOrderId());

      eventPublisher.publish(event);

      log.info("=== 무료 결제 이벤트 발행 완료 === orderId: {}", freePaymentCompletionResult.getOrderId());

    } catch (Exception e) {
      log.error("무료 결제 이벤트 발행 중 예외 발생 - orderId: {}", freePaymentCompletionResult.getOrderId(), e);
      throw e;
    }
  }

  /** 사용자 타입 문자열 반환 */
  protected String getUserTypeString(UserContext userContext) {
    return userContext.isMember() ? "회원" : "비회원";
  }
}
