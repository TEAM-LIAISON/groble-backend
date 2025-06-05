package liaison.groble.application.order;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.content.ContentReader;
import liaison.groble.application.order.dto.CreateOrderDto;
import liaison.groble.application.order.dto.CreateOrderResponse;
import liaison.groble.application.order.dto.OrderSuccessResponse;
import liaison.groble.application.order.dto.ValidatedOrderOptionDto;
import liaison.groble.application.user.service.UserReader;
import liaison.groble.domain.content.entity.Content;
import liaison.groble.domain.content.entity.ContentOption;
import liaison.groble.domain.coupon.entity.UserCoupon;
import liaison.groble.domain.coupon.repository.UserCouponRepository;
import liaison.groble.domain.order.entity.Order;
import liaison.groble.domain.order.entity.OrderItem;
import liaison.groble.domain.order.repository.OrderRepository;
import liaison.groble.domain.order.vo.OrderOptionInfo;
import liaison.groble.domain.purchase.entity.Purchase;
import liaison.groble.domain.purchase.entity.Purchaser;
import liaison.groble.domain.purchase.repository.PurchaseRepository;
import liaison.groble.domain.user.entity.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
  private final UserReader userReader;
  private final ContentReader contentReader;
  private final OrderRepository orderRepository;
  private final UserCouponRepository userCouponRepository;
  private final PurchaseRepository purchaseRepository;

  /**
   * 회원 주문 생성
   *
   * <p>이 메서드는 사용자가 선택한 콘텐츠와 옵션들로 주문을 생성하고, 쿠폰이 있는 경우 최적의 쿠폰을 자동으로 적용합니다.
   *
   * @param createOrderDto 주문 생성 요청 정보
   * @param userId 주문하는 사용자 ID
   * @return 생성된 주문 정보
   */
  @Transactional
  public CreateOrderResponse createOrderForUser(CreateOrderDto createOrderDto, Long userId) {
    // 1단계: 사용자와 콘텐츠 조회
    User user = userReader.getUserById(userId);
    Content content = contentReader.getContentById(createOrderDto.getContentId());

    // 2단계: 각 옵션의 유효성 검증 및 가격 정보 조회
    // 클라이언트에서 전달받은 옵션 정보를 검증하고, 실제 가격을 데이터베이스에서 조회합니다
    List<ValidatedOrderOptionDto> validatedOptions =
        validateAndEnrichOptions(content, createOrderDto.getOptions());

    // 3단계: Application DTO를 Domain 값 객체로 변환
    List<OrderOptionInfo> domainOptions =
        validatedOptions.stream()
            .map(
                option ->
                    OrderOptionInfo.builder()
                        .optionId(option.getOptionId())
                        .optionType(option.getOptionType())
                        .price(option.getPrice())
                        .quantity(option.getQuantity())
                        .build())
            .collect(Collectors.toList());

    // 4단계: 구매자 정보 생성
    Purchaser purchaser =
        Purchaser.builder()
            .name(user.getNickname())
            .email(user.getEmail())
            .phone(user.getPhoneNumber())
            .build();

    // 5단계: 주문 생성 - 팩토리 메서드 사용
    Order order = Order.createOrderWithMultipleOptions(user, content, domainOptions, purchaser);

    // 6단계: 주문 저장 및 merchantUid 생성
    order = orderRepository.save(order);
    order.setMerchantUid(Order.generateMerchantUid(order.getId()));

    // 7단계: 쿠폰 적용 처리
    UserCoupon appliedCoupon = null;
    if (createOrderDto.getCouponCodes() != null && !createOrderDto.getCouponCodes().isEmpty()) {
      // 상품 원가가 0원이면 쿠폰 적용 불가
      if (order.getOriginalPrice().compareTo(BigDecimal.ZERO) > 0) {
        appliedCoupon =
            findAndValidateBestCoupon(
                user, createOrderDto.getCouponCodes(), order.getOriginalPrice());

        if (appliedCoupon != null) {
          order.applyCoupon(appliedCoupon);
          log.info(
              "쿠폰 적용 완료 - orderId: {}, couponCode: {}, 할인금액: {}",
              order.getId(),
              appliedCoupon.getCouponCode(),
              order.getCouponDiscountPrice());
        }
      } else {
        log.info("상품 원가가 0원이므로 쿠폰 적용이 불가능합니다 - orderId: {}", order.getId());
      }
    }

    // 8단계: 최종 주문 저장 및 무료 상품 처리
    order = orderRepository.save(order);

    boolean isPurchasedContent = false;

    // 최종 금액이 0원이면 즉시 구매 처리
    if (order.getFinalPrice().compareTo(BigDecimal.ZERO) == 0) {
      isPurchasedContent = processFreeOrderPurchase(order);
      log.info("무료 주문으로 즉시 구매 처리 완료 - orderId: {}", order.getId());
    }

    log.info(
        "주문 생성 완료 - orderId: {}, userId: {}, contentId: {}, 옵션수: {}, 최종금액: {}, 즉시구매: {}",
        order.getId(),
        userId,
        createOrderDto.getContentId(),
        validatedOptions.size(),
        order.getFinalPrice(),
        isPurchasedContent);

    return buildCreateOrderResponse(order, isPurchasedContent);
  }

  /**
   * 옵션 검증 및 가격 정보 추가
   *
   * <p>이 메서드는 클라이언트에서 전달받은 옵션 정보를 검증하고, 실제 데이터베이스에서 가격 정보를 조회하여 ValidatedOrderOptionDto로 변환합니다.
   *
   * <p>이 메서드의 역할: 1. 옵션이 해당 콘텐츠에 속하는지 검증 2. 옵션 타입이 올바른지 검증 3. 데이터베이스에서 실제 가격 조회 4. 검증된 정보를 담은 DTO
   * 생성
   */
  private List<ValidatedOrderOptionDto> validateAndEnrichOptions(
      Content content, List<CreateOrderDto.OrderOptionDto> requestedOptions) {

    List<ValidatedOrderOptionDto> validatedOptions = new ArrayList<>();

    for (CreateOrderDto.OrderOptionDto requestedOption : requestedOptions) {
      // 콘텐츠에서 해당 옵션 찾기
      ContentOption contentOption =
          content.getOptions().stream()
              .filter(opt -> opt.getId().equals(requestedOption.getOptionId()))
              .findFirst()
              .orElseThrow(
                  () ->
                      new IllegalArgumentException(
                          "옵션을 찾을 수 없습니다: " + requestedOption.getOptionId()));

      // 검증된 옵션 DTO 생성
      validatedOptions.add(
          ValidatedOrderOptionDto.builder()
              .optionId(contentOption.getId())
              .optionType(OrderItem.OptionType.valueOf(requestedOption.getOptionType().name()))
              .price(contentOption.getPrice())
              .quantity(requestedOption.getQuantity())
              .build());
    }

    return validatedOptions;
  }

  /**
   * 가장 유리한 쿠폰 찾기 및 검증
   *
   * <p>여러 쿠폰 중 할인 금액이 가장 큰 쿠폰을 선택합니다. 각 쿠폰의 사용 가능 여부를 검증하고, 할인 금액을 계산하여 비교합니다.
   */
  private UserCoupon findAndValidateBestCoupon(
      User user, List<String> couponCodes, BigDecimal orderPrice) {
    UserCoupon bestCoupon = null;
    BigDecimal maxDiscount = BigDecimal.ZERO;

    for (String couponCode : couponCodes) {
      try {
        UserCoupon coupon = userCouponRepository.findByCouponCode(couponCode).orElse(null);

        if (coupon == null) {
          log.warn("쿠폰을 찾을 수 없습니다: {}", couponCode);
          continue;
        }

        // 쿠폰 소유자 확인
        if (!coupon.getUser().getId().equals(user.getId())) {
          log.warn("본인 소유가 아닌 쿠폰: {}", couponCode);
          continue;
        }

        // 쿠폰 사용 가능 여부 확인
        if (!coupon.isUsable()) {
          log.warn("사용할 수 없는 쿠폰: {}", couponCode);
          continue;
        }

        // 할인 금액 계산
        BigDecimal discountPrice = coupon.getCouponTemplate().calculateDiscountPrice(orderPrice);

        // 가장 할인이 큰 쿠폰 선택
        if (discountPrice.compareTo(maxDiscount) > 0) {
          maxDiscount = discountPrice;
          bestCoupon = coupon;
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
   * 비회원 주문 생성
   *
   * <p>로그인하지 않은 사용자도 이메일과 전화번호로 주문을 생성할 수 있습니다. 쿠폰은 회원만 사용할 수 있으므로 적용되지 않습니다.
   *
   * @param createOrderDto 주문 생성 요청 정보 (email, phoneNumber 포함)
   * @return 생성된 주문 정보
   */
  @Transactional
  public CreateOrderResponse createPublicOrder(CreateOrderDto createOrderDto) {
    // 1단계: 비회원 주문 필수 정보 검증
    if (createOrderDto.getEmail() == null || createOrderDto.getEmail().isBlank()) {
      throw new IllegalArgumentException("비회원 주문 시 이메일은 필수입니다.");
    }
    if (createOrderDto.getPhoneNumber() == null || createOrderDto.getPhoneNumber().isBlank()) {
      throw new IllegalArgumentException("비회원 주문 시 전화번호는 필수입니다.");
    }

    // 2단계: 콘텐츠 조회
    Content content = contentReader.getContentById(createOrderDto.getContentId());

    // 3단계: 각 옵션의 유효성 검증 및 가격 정보 조회
    List<ValidatedOrderOptionDto> validatedOptions =
        validateAndEnrichOptions(content, createOrderDto.getOptions());

    // 4단계: Application DTO를 Domain 값 객체로 변환
    List<OrderOptionInfo> domainOptions =
        validatedOptions.stream()
            .map(
                option ->
                    OrderOptionInfo.builder()
                        .optionId(option.getOptionId())
                        .price(option.getPrice())
                        .quantity(option.getQuantity())
                        .build())
            .collect(Collectors.toList());

    // 5단계: 구매자 정보 생성 (DTO에서 직접 사용)
    Purchaser purchaser =
        Purchaser.builder()
            .name("Guest") // 비회원은 기본 이름 사용
            .email(createOrderDto.getEmail())
            .phone(createOrderDto.getPhoneNumber())
            .build();

    // 6단계: 비회원 주문 생성 - 새로 추가한 팩토리 메서드 사용
    Order order = Order.createPublicOrderWithMultipleOptions(content, domainOptions, purchaser);

    // 7단계: 주문 저장 및 merchantUid 생성
    order = orderRepository.save(order);
    order.setMerchantUid(Order.generateMerchantUid(order.getId()));

    // 8단계: 쿠폰은 회원 전용이므로 로그만 출력
    if (createOrderDto.getCouponCodes() != null && !createOrderDto.getCouponCodes().isEmpty()) {
      log.warn("비회원 주문에서 쿠폰 코드가 전달되었으나 무시됩니다: {}", createOrderDto.getCouponCodes());
    }

    // 9단계: 최종 주문 저장 및 무료 상품 처리
    order = orderRepository.save(order);

    boolean isPurchasedContent = false;

    // 최종 금액이 0원이면 즉시 구매 처리
    if (order.getFinalPrice().compareTo(BigDecimal.ZERO) == 0) {
      isPurchasedContent = processFreeOrderPurchase(order);
      log.info("무료 비회원 주문으로 즉시 구매 처리 완료 - orderId: {}", order.getId());
    }

    log.info(
        "비회원 주문 생성 완료 - orderId: {}, email: {}, contentId: {}, 옵션수: {}, 최종금액: {}, 즉시구매: {}",
        order.getId(),
        createOrderDto.getEmail(),
        createOrderDto.getContentId(),
        validatedOptions.size(),
        order.getFinalPrice(),
        isPurchasedContent);

    return buildPublicOrderResponse(order, isPurchasedContent);
  }

  /**
   * CreateOrderResponse 생성 (회원 주문용)
   *
   * <p>생성된 주문 정보를 바탕으로 클라이언트에 반환할 응답을 구성합니다.
   */
  private CreateOrderResponse buildCreateOrderResponse(Order order, boolean isPurchasedContent) {
    return CreateOrderResponse.builder()
        .merchantUid(order.getMerchantUid())
        .email(order.getUser().getEmail())
        .phoneNumber(order.getUser().getPhoneNumber())
        .contentTitle(order.getOrderItems().get(0).getContent().getTitle())
        .totalPrice(order.getTotalPrice())
        .isPurchasedContent(isPurchasedContent)
        .build();
  }

  /**
   * CreateOrderResponse 생성 (비회원 주문용)
   *
   * <p>비회원 주문의 경우 Purchaser 정보를 사용하여 응답을 구성합니다.
   */
  private CreateOrderResponse buildPublicOrderResponse(Order order, boolean isPurchasedContent) {
    return CreateOrderResponse.builder()
        .merchantUid(order.getMerchantUid())
        .email(order.getPurchaser().getEmail())
        .phoneNumber(order.getPurchaser().getPhone())
        .contentTitle(order.getOrderItems().get(0).getContent().getTitle())
        .totalPrice(order.getTotalPrice())
        .isPurchasedContent(isPurchasedContent)
        .build();
  }

  /**
   * 무료 주문에 대한 구매 처리
   *
   * <p>PayplePaymentService의 결제 승인 로직을 참고하여 무료 주문에 대해 즉시 구매 처리를 수행합니다.
   *
   * @param order 무료 주문 (최종 금액이 0원)
   * @return 구매 처리 성공 여부
   */
  private boolean processFreeOrderPurchase(Order order) {
    try {
      // 1. Order 상태 업데이트 (결제 완료 + 쿠폰 사용 처리)
      // 무료 주문이므로 Payment 엔티티는 생성하지 않음
      order.completePayment();
      orderRepository.save(order);

      // 2. Purchase 생성 및 즉시 완료 처리
      Purchase purchase = Purchase.createFromOrder(order);
      purchase.complete(); // PENDING → COMPLETED 상태 변경 및 purchasedAt 설정
      purchaseRepository.save(purchase);

      log.info(
          "무료 주문 구매 처리 성공 - orderId: {}, purchaseId: {}, userId: {}, contentId: {}",
          order.getId(),
          purchase.getId(),
          order.getUser() != null ? order.getUser().getId() : "guest",
          purchase.getContent().getId());

      return true;

    } catch (Exception e) {
      log.error("무료 주문 구매 처리 실패 - orderId: {}", order.getId(), e);

      // 실패 시 주문 상태를 실패로 변경
      try {
        order.failOrder("무료 구매 처리 실패: " + e.getMessage());
        orderRepository.save(order);
      } catch (Exception failException) {
        log.error("주문 실패 처리 중 추가 오류 발생 - orderId: {}", order.getId(), failException);
      }

      return false;
    }
  }

  /**
   * 결제 성공 후 주문 정보 조회
   *
   * <p>merchantUid를 기반으로 주문 정보를 조회하고, 해당 주문의 상품 정보와 결제 정보를 반환합니다.
   *
   * @param merchantUid 주문 고유 식별자
   * @param userId 요청한 사용자 ID (권한 검증용)
   * @return 주문 성공 정보
   */
  @Transactional(readOnly = true)
  public OrderSuccessResponse getOrderSuccess(String merchantUid, Long userId) {
    // 1. 주문 조회
    Order order =
        orderRepository
            .findByMerchantUid(merchantUid)
            .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + merchantUid));

    // 2. 권한 검증 (본인의 주문인지 확인)
    if (order.getUser() == null || !order.getUser().getId().equals(userId)) {
      throw new IllegalStateException("해당 주문에 대한 접근 권한이 없습니다.");
    }

    // 3. 주문 상태 검증 (결제 완료 상태인지 확인)
    if (order.getStatus() != Order.OrderStatus.PAID) {
      throw new IllegalStateException("완료되지 않은 주문입니다. 현재 상태: " + order.getStatus());
    }

    // 4. Purchase 정보 조회
    Purchase purchase =
        purchaseRepository
            .findByOrder(order)
            .orElseThrow(() -> new IllegalStateException("구매 정보를 찾을 수 없습니다."));

    // 5. 응답 생성
    return buildOrderSuccessResponse(order, purchase);
  }

  /**
   * OrderSuccessResponse 생성
   *
   * <p>주문과 구매 정보를 바탕으로 결제 성공 응답을 구성합니다.
   */
  private OrderSuccessResponse buildOrderSuccessResponse(Order order, Purchase purchase) {
    // 첫 번째 주문 아이템 정보 (단일 콘텐츠 구매 가정)
    OrderItem orderItem = order.getOrderItems().get(0);
    Content content = orderItem.getContent();

    return OrderSuccessResponse.builder()
        .merchantUid(order.getMerchantUid())
        .orderNumber(order.getId().toString())
        .orderStatus(order.getStatus().name())
        .purchaseStatus(purchase.getStatus().name())
        .contentId(content.getId())
        .contentTitle(content.getTitle())
        .contentThumbnailUrl(content.getThumbnailUrl())
        .selectedOptionId(orderItem.getOptionId())
        .selectedOptionType(
            orderItem.getOptionType() != null ? orderItem.getOptionType().name() : null)
        .originalPrice(order.getOriginalPrice())
        .discountPrice(order.getDiscountPrice())
        .couponDiscountPrice(order.getCouponDiscountPrice())
        .finalPrice(order.getFinalPrice())
        .appliedCouponCode(
            order.getAppliedCoupon() != null ? order.getAppliedCoupon().getCouponCode() : null)
        .purchasedAt(purchase.getPurchasedAt())
        .isFreePurchase(order.getFinalPrice().compareTo(BigDecimal.ZERO) == 0)
        .build();
  }
}
