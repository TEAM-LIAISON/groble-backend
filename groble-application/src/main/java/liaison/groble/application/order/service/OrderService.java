package liaison.groble.application.order.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.content.ContentReader;
import liaison.groble.application.order.dto.CreateOrderDto;
import liaison.groble.application.order.dto.CreateOrderResponse;
import liaison.groble.application.order.dto.OrderSuccessResponse;
import liaison.groble.application.order.dto.ValidatedOrderOptionDto;
import liaison.groble.application.purchase.service.PurchaseReader;
import liaison.groble.application.user.service.UserReader;
import liaison.groble.domain.content.entity.Content;
import liaison.groble.domain.content.entity.ContentOption;
import liaison.groble.domain.coupon.entity.UserCoupon;
import liaison.groble.domain.coupon.repository.UserCouponRepository;
import liaison.groble.domain.order.entity.Order;
import liaison.groble.domain.order.entity.OrderItem;
import liaison.groble.domain.order.entity.Purchaser;
import liaison.groble.domain.order.repository.OrderRepository;
import liaison.groble.domain.order.vo.OrderOptionInfo;
import liaison.groble.domain.payment.entity.Payment;
import liaison.groble.domain.payment.repository.PaymentRepository;
import liaison.groble.domain.purchase.entity.Purchase;
import liaison.groble.domain.purchase.repository.PurchaseRepository;
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
  private final ContentReader contentReader;
  private final PurchaseReader purchaseReader;
  private final OrderRepository orderRepository;
  private final UserCouponRepository userCouponRepository;
  private final PurchaseRepository purchaseRepository;
  private final PaymentRepository paymentRepository;

  /**
   * 사용자를 위한 주문 생성
   *
   * <p>주문 생성 프로세스:
   *
   * <ol>
   *   <li>사용자 및 콘텐츠 정보 조회
   *   <li>주문 옵션 검증 (콘텐츠에 해당 옵션이 존재하는지 확인)
   *   <li>주문 생성 및 고유 번호(merchantUid) 할당
   *   <li>쿠폰 적용 (여러 쿠폰 중 최대 할인 쿠폰 자동 선택)
   *   <li>무료 주문인 경우 즉시 구매 완료 처리
   * </ol>
   *
   * @param dto 주문 생성 요청 정보 (콘텐츠ID, 옵션 정보, 쿠폰 코드 등)
   * @param userId 주문을 생성하는 사용자 ID
   * @return 생성된 주문 정보 응답
   * @throws IllegalArgumentException 사용자나 콘텐츠를 찾을 수 없는 경우
   * @throws IllegalArgumentException 요청한 옵션이 콘텐츠에 존재하지 않는 경우
   */
  @Transactional
  public CreateOrderResponse createOrderForUser(CreateOrderDto dto, Long userId) {
    log.info("주문 생성 시작 - userId: {}, contentId: {}", userId, dto.getContentId());

    // 1. 주문에 필요한 기본 정보 조회
    final User user = userReader.getUserById(userId);
    final Content content = contentReader.getContentById(dto.getContentId());

    // 2. 주문 옵션 검증 및 변환
    final List<ValidatedOrderOptionDto> validatedOptions =
        validateAndEnrichOptions(content, dto.getOptions());
    final List<OrderOptionInfo> orderOptions = convertToDomainOptions(validatedOptions);

    // 3. 구매자 정보 생성
    final Purchaser purchaser = buildPurchaserFromUser(user);

    // 4. 주문 생성 및 저장
    Order order = createAndSaveOrder(user, content, orderOptions, purchaser);

    // 5. 쿠폰 적용 (요청된 경우)
    applyCouponIfRequested(order, user, dto.getCouponCodes());

    // 6. 무료 주문 처리 (최종 금액이 0원인 경우)
    final boolean isFreePurchase = processIfFreeOrder(order);

    // 7. 주문 생성 로그 기록
    logOrderCreation(order, userId, dto.getContentId(), validatedOptions.size(), isFreePurchase);

    // 8. 응답 생성
    return buildCreateOrderResponse(order, isFreePurchase);
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
  private List<ValidatedOrderOptionDto> validateAndEnrichOptions(
      Content content, List<CreateOrderDto.OrderOptionDto> requestedOptions) {

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
  private ValidatedOrderOptionDto validateAndEnrichSingleOption(
      Content content, CreateOrderDto.OrderOptionDto requestedOption) {

    // 콘텐츠에서 해당 옵션 찾기
    ContentOption matchedOption = findContentOption(content, requestedOption.getOptionId());

    // 수량 검증
    if (requestedOption.getQuantity() <= 0) {
      throw new IllegalArgumentException(
          "옵션 수량은 1개 이상이어야 합니다. optionId=" + requestedOption.getOptionId());
    }

    return ValidatedOrderOptionDto.builder()
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
      CreateOrderDto.OrderOptionDto.OptionType clientOptionType) {
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
   * 쿠폰 적용 처리
   *
   * <p>요청된 쿠폰들 중 가장 할인이 큰 쿠폰을 자동으로 선택하여 적용합니다. 무료 상품(원가 0원)에는 쿠폰을 적용할 수 없습니다.
   *
   * @param order 주문
   * @param user 사용자
   * @param couponCodes 적용하려는 쿠폰 코드 목록
   */
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

      // 2. Purchase 생성 및 완료 처리
      Purchase purchase = createAndCompletePurchase(order);

      // 3. 주문 상태를 결제 완료로 변경
      order.completePayment();
      orderRepository.save(order);

      log.info(
          "무료 주문 구매 처리 성공 - orderId: {}, paymentId: {}, purchaseId: {}, userId: {}, contentId: {}",
          order.getId(),
          freePayment.getId(),
          purchase.getId(),
          order.getUser().getId(),
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
    return paymentRepository.save(freePayment);
  }

  /**
   * 구매 정보 생성 및 완료 처리
   *
   * @param order 주문
   * @return 생성된 구매 정보
   */
  private Purchase createAndCompletePurchase(Order order) {
    Purchase purchase = Purchase.createFromOrder(order);
    purchase.complete(); // PENDING → COMPLETED 상태 변경
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
   * 결제 성공 후 주문 정보 조회
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
  public OrderSuccessResponse getOrderSuccess(String merchantUid, Long userId) {
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
   * 주문 접근 권한 검증
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
   * 주문 응답 DTO 생성 (회원 주문용)
   *
   * @param order 주문
   * @param isPurchasedContent 구매 완료 여부 (무료 주문인 경우 true)
   * @return 주문 생성 응답
   */
  private CreateOrderResponse buildCreateOrderResponse(Order order, boolean isPurchasedContent) {
    // 첫 번째 주문 항목의 콘텐츠 제목 (현재는 단일 콘텐츠 구매만 지원)
    String contentTitle = order.getOrderItems().get(0).getContent().getTitle();

    return CreateOrderResponse.builder()
        .merchantUid(order.getMerchantUid())
        .email(order.getUser().getEmail())
        .phoneNumber(order.getUser().getPhoneNumber())
        .contentTitle(contentTitle)
        .totalPrice(order.getTotalPrice())
        .isPurchasedContent(isPurchasedContent)
        .build();
  }

  /**
   * 주문 응답 DTO 생성 (비회원 주문용)
   *
   * <p>비회원 주문의 경우 Purchaser 정보를 사용하여 응답을 구성합니다. 현재는 사용되지 않지만, 향후 비회원 구매 기능 추가 시 사용될 예정입니다.
   *
   * @param order 주문
   * @param isPurchasedContent 구매 완료 여부
   * @return 주문 생성 응답
   */
  private CreateOrderResponse buildPublicOrderResponse(Order order, boolean isPurchasedContent) {
    String contentTitle = order.getOrderItems().get(0).getContent().getTitle();

    return CreateOrderResponse.builder()
        .merchantUid(order.getMerchantUid())
        .email(order.getPurchaser().getEmail())
        .phoneNumber(order.getPurchaser().getPhone())
        .contentTitle(contentTitle)
        .totalPrice(order.getTotalPrice())
        .isPurchasedContent(isPurchasedContent)
        .build();
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
  private OrderSuccessResponse buildOrderSuccessResponse(Order order, Purchase purchase) {
    // 첫 번째 주문 아이템 정보 (현재는 단일 콘텐츠 구매만 지원)
    OrderItem orderItem = order.getOrderItems().get(0);
    Content content = orderItem.getContent();

    return OrderSuccessResponse.builder()
        // 주문 기본 정보
        .merchantUid(order.getMerchantUid())
        .purchasedAt(purchase.getPurchasedAt())
        .contentId(content.getId())
        .contentTitle(content.getTitle())
        .orderStatus(order.getStatus().name())

        // 콘텐츠 정보

        .contentThumbnailUrl(content.getThumbnailUrl())

        // 선택한 옵션 정보
        .selectedOptionId(orderItem.getOptionId())
        .selectedOptionType(
            orderItem.getOptionType() != null ? orderItem.getOptionType().name() : null)

        // 가격 정보
        .originalPrice(order.getOriginalPrice())
        .discountPrice(order.getDiscountPrice())
        .finalPrice(order.getFinalPrice())

        // 구매 정보

        .isFreePurchase(order.getFinalPrice().compareTo(BigDecimal.ZERO) == 0)
        .build();
  }

  /**
   * 검증된 옵션 정보를 도메인 옵션 정보로 변환
   *
   * <p>검증이 완료된 옵션 정보를 도메인 엔티티에서 사용할 수 있는 형태로 변환합니다.
   *
   * @param options 검증된 옵션 목록
   * @return 도메인 옵션 정보 목록
   */
  private List<OrderOptionInfo> convertToDomainOptions(List<ValidatedOrderOptionDto> options) {
    return options.stream().map(this::convertSingleOption).collect(Collectors.toList());
  }

  /**
   * 단일 옵션 변환
   *
   * @param option 검증된 옵션
   * @return 도메인 옵션 정보
   */
  private OrderOptionInfo convertSingleOption(ValidatedOrderOptionDto option) {
    return OrderOptionInfo.builder()
        .optionId(option.getOptionId())
        .optionType(option.getOptionType())
        .price(option.getPrice())
        .quantity(option.getQuantity())
        .build();
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
   * 주문 생성 로그 기록
   *
   * <p>주문 생성 완료 시 주요 정보를 로그로 기록합니다. 모니터링 및 디버깅에 활용됩니다.
   *
   * @param order 생성된 주문
   * @param userId 사용자 ID
   * @param contentId 콘텐츠 ID
   * @param optionCount 선택한 옵션 개수
   * @param isFree 무료 주문 여부
   */
  private void logOrderCreation(
      Order order, Long userId, Long contentId, int optionCount, boolean isFree) {
    log.info(
        "주문 생성 완료 - orderId: {}, merchantUid: {}, userId: {}, contentId: {}, "
            + "옵션수: {}, 원가: {}원, 최종금액: {}원, 할인금액: {}원, 즉시구매: {}",
        order.getId(),
        order.getMerchantUid(),
        userId,
        contentId,
        optionCount,
        order.getOriginalPrice(),
        order.getFinalPrice(),
        order.getDiscountPrice(),
        isFree);
  }
}
