package liaison.groble.application.order;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.content.ContentReader;
import liaison.groble.application.order.dto.CreateFinalizeOrderDto;
import liaison.groble.application.order.dto.CreateInitialOrderDto;
import liaison.groble.application.order.dto.FinalizeOrderResponse;
import liaison.groble.application.order.dto.InitialOrderResponse;
import liaison.groble.application.order.dto.ValidatedOrderOptionDto;
import liaison.groble.application.order.service.OrderReader;
import liaison.groble.application.user.service.UserReader;
import liaison.groble.domain.content.entity.CoachingOption;
import liaison.groble.domain.content.entity.Content;
import liaison.groble.domain.content.entity.ContentOption;
import liaison.groble.domain.content.entity.DocumentOption;
import liaison.groble.domain.coupon.entity.UserCoupon;
import liaison.groble.domain.coupon.repository.UserCouponRepository;
import liaison.groble.domain.order.entity.Order;
import liaison.groble.domain.order.entity.OrderItem;
import liaison.groble.domain.order.repository.OrderRepository;
import liaison.groble.domain.order.vo.OrderOptionInfo;
import liaison.groble.domain.purchase.entity.Purchaser;
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
  private final OrderReader orderReader;

  /**
   * 초기 주문 생성 - 여러 옵션 지원
   *
   * <p>이 메서드는 사용자가 한 콘텐츠에서 여러 옵션을 선택하여 구매할 수 있도록 합니다. 예를 들어, 코칭 옵션 A를 2개, 코칭 옵션 B를 3개와 같이 장바구니 형태의
   * 주문을 생성합니다.
   */
  @Transactional
  public InitialOrderResponse createInitialOrder(CreateInitialOrderDto dto) {
    // 1단계: 사용자와 콘텐츠 조회
    User user = userReader.getUserById(dto.getUserId());
    Content content = contentReader.getContentById(dto.getContentId());

    // 2단계: 각 옵션의 유효성 검증 및 가격 정보 조회
    // 클라이언트에서 전달받은 옵션 정보를 검증하고, 실제 가격을 데이터베이스에서 조회합니다
    List<ValidatedOrderOptionDto> validatedOptions =
        validateAndEnrichOptions(content, dto.getOptions());

    // 3단계: Application DTO를 Domain 값 객체로 변환
    // 이 변환 과정이 중요한 이유:
    // - Domain 계층은 Application 계층의 DTO를 알 필요가 없습니다
    // - 각 계층은 자신만의 모델을 가지며, 계층 간 변환을 통해 의존성을 관리합니다
    // - 이를 통해 각 계층을 독립적으로 변경할 수 있습니다
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
    // 초기 주문에서는 사용자의 기본 정보를 사용합니다
    Purchaser purchaser =
        Purchaser.builder()
            .name(user.getNickname())
            .email(user.getEmail())
            .phone(user.getPhoneNumber())
            .build();

    // 5단계: 주문 생성 - 팩토리 메서드 사용
    Order order =
        Order.createOrderWithMultipleOptions(
            user,
            content,
            domainOptions, // Domain 값 객체 사용
            purchaser);

    // 6단계: 주문 저장 및 merchantUid 생성
    order = orderRepository.save(order);
    order.setMerchantUid(Order.generateMerchantUid(order.getId()));

    log.info(
        "초기 주문 생성 완료 - orderId: {}, userId: {}, contentId: {}, 옵션수: {}, 총액: {}",
        order.getId(),
        dto.getUserId(),
        dto.getContentId(),
        validatedOptions.size(),
        order.getFinalPrice());

    // 7단계: 응답 생성
    return buildInitialOrderResponse(order);
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
      Content content, List<CreateInitialOrderDto.OrderOptionDto> requestedOptions) {

    List<ValidatedOrderOptionDto> validatedOptions = new ArrayList<>();

    for (CreateInitialOrderDto.OrderOptionDto requestedOption : requestedOptions) {
      // 콘텐츠에서 해당 옵션 찾기
      ContentOption contentOption =
          content.getOptions().stream()
              .filter(opt -> opt.getId().equals(requestedOption.getOptionId()))
              .findFirst()
              .orElseThrow(
                  () ->
                      new IllegalArgumentException(
                          "옵션을 찾을 수 없습니다: " + requestedOption.getOptionId()));

      // 옵션 타입 검증 - 요청된 타입과 실제 타입이 일치하는지 확인
      validateOptionType(contentOption, requestedOption.getOptionType());

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
   * 옵션 타입 검증
   *
   * <p>ContentOption의 실제 타입과 요청된 타입이 일치하는지 확인합니다. 예를 들어, CoachingOption인데 DOCUMENT_OPTION으로 요청하면
   * 예외를 발생시킵니다.
   */
  private void validateOptionType(
      ContentOption contentOption, CreateInitialOrderDto.OptionType requestedType) {
    boolean isValid = false;

    if (contentOption instanceof CoachingOption
        && requestedType == CreateInitialOrderDto.OptionType.COACHING_OPTION) {
      isValid = true;
    } else if (contentOption instanceof DocumentOption
        && requestedType == CreateInitialOrderDto.OptionType.DOCUMENT_OPTION) {
      isValid = true;
    }

    if (!isValid) {
      throw new IllegalArgumentException(
          String.format(
              "옵션 타입이 일치하지 않습니다. 옵션ID: %d, 요청타입: %s", contentOption.getId(), requestedType));
    }
  }

  /**
   * InitialOrderResponse 생성
   *
   * <p>생성된 주문 정보를 바탕으로 클라이언트에 반환할 응답을 구성합니다.
   */
  private InitialOrderResponse buildInitialOrderResponse(Order order) {
    List<InitialOrderResponse.OrderItemResponse> orderItemResponses =
        order.getOrderItems().stream()
            .map(
                item ->
                    InitialOrderResponse.OrderItemResponse.builder()
                        .optionId(item.getOptionId())
                        .optionType(item.getOptionType().name())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .totalPrice(item.getTotalPrice())
                        .build())
            .collect(Collectors.toList());

    return InitialOrderResponse.builder()
        .orderId(order.getId())
        .merchantUid(order.getMerchantUid())
        .originalPrice(order.getOriginalPrice())
        .discountPrice(order.getDiscountPrice())
        .finalPrice(order.getFinalPrice())
        .orderItems(orderItemResponses)
        .build();
  }

  /**
   * 최종 주문 확정 메서드 - 쿠폰 적용 및 결제 준비
   *
   * <p>이 메서드는 초기 주문(Initial Order)을 최종 확정하는 단계입니다. 주요 기능: 1. 기존 주문 검증 2. 쿠폰 적용 (여러 쿠폰 중 최적 선택) 3.
   * 최종 금액 확정 4. 결제 준비 상태로 전환
   */
  @Transactional
  public FinalizeOrderResponse createFinalizeOrder(CreateFinalizeOrderDto dto) {
    // 1단계: 필요한 엔티티 조회
    User user = userReader.getUserById(dto.getUserId());
    Content content = contentReader.getContentById(dto.getContentId());

    // merchantUid로 기존 주문 조회 - 초기 주문이 이미 생성되어 있어야 함
    Order order = orderReader.getOrderByMerchantUid(dto.getMerchantUid());

    // 2단계: 주문 상태 및 소유권 검증
    validateOrderForFinalization(order, user, content);

    // 3단계: 요청된 옵션과 기존 주문 항목 비교 검증
    // 초기 주문과 최종 주문의 옵션이 일치하는지 확인
    validateOrderItemsMatch(order, dto.getOptions());

    // 4단계: 쿠폰 검증 및 적용
    // 여러 쿠폰 중 가장 유리한 하나만 적용하는 것이 일반적
    UserCoupon appliedCoupon = null;
    if (dto.getCouponCodes() != null && !dto.getCouponCodes().isEmpty()) {
      appliedCoupon =
          findAndValidateBestCoupon(user, dto.getCouponCodes(), order.getOriginalPrice());

      if (appliedCoupon != null) {
        order.applyCoupon(appliedCoupon);
        log.info(
            "쿠폰 적용 완료 - orderId: {}, couponCode: {}, 할인금액: {}",
            order.getId(),
            appliedCoupon.getCouponCode(),
            order.getCouponDiscountPrice());
      }
    }

    // 5단계: 주문 저장
    order = orderRepository.save(order);

    // 6단계: 응답 생성
    return buildFinalizeOrderResponse(order, appliedCoupon);
  }

  /** 주문 최종화 가능 여부 검증 */
  private void validateOrderForFinalization(Order order, User user, Content content) {
    // 주문 상태 확인 - PENDING 상태에서만 최종화 가능
    if (order.getStatus() != Order.OrderStatus.PENDING) {
      throw new IllegalStateException(
          String.format("주문 상태가 올바르지 않습니다. 현재 상태: %s", order.getStatus()));
    }

    // 주문 소유자 확인
    if (!order.getUser().getId().equals(user.getId())) {
      throw new IllegalArgumentException("본인의 주문만 확정할 수 있습니다");
    }

    // 콘텐츠 일치 확인
    boolean contentMatches =
        order.getOrderItems().stream()
            .allMatch(item -> item.getContent().getId().equals(content.getId()));

    if (!contentMatches) {
      throw new IllegalArgumentException("주문의 콘텐츠와 요청한 콘텐츠가 일치하지 않습니다");
    }
  }

  /**
   * 요청된 옵션과 기존 주문 항목 비교
   *
   * <p>초기 주문 생성 시의 옵션과 최종 확정 시의 옵션이 일치하는지 검증합니다. 이는 주문 무결성을 보장하기 위한 중요한 검증입니다.
   */
  private void validateOrderItemsMatch(
      Order order, List<CreateFinalizeOrderDto.OrderOptionDto> requestedOptions) {
    // 옵션 개수 확인
    if (order.getOrderItems().size() != requestedOptions.size()) {
      throw new IllegalArgumentException("주문 항목 수가 일치하지 않습니다");
    }

    // 각 옵션의 상세 내용 확인
    for (CreateFinalizeOrderDto.OrderOptionDto requested : requestedOptions) {
      boolean found =
          order.getOrderItems().stream()
              .anyMatch(
                  item ->
                      item.getOptionId().equals(requested.getOptionId())
                          && item.getOptionType().name().equals(requested.getOptionType().name())
                          && item.getQuantity() == requested.getQuantity());

      if (!found) {
        throw new IllegalArgumentException(
            String.format("주문 항목이 일치하지 않습니다. 옵션ID: %d", requested.getOptionId()));
      }
    }
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
   * FinalizeOrderResponse 생성
   *
   * <p>확정된 주문 정보를 바탕으로 클라이언트에 반환할 응답을 구성합니다. 쿠폰 적용 정보와 최종 금액을 포함합니다.
   */
  private FinalizeOrderResponse buildFinalizeOrderResponse(Order order, UserCoupon appliedCoupon) {
    // 주문 항목 정보 구성
    List<FinalizeOrderResponse.OrderItemInfo> orderItems =
        order.getOrderItems().stream()
            .map(
                item -> {
                  // 옵션명 조회
                  String optionName =
                      getOptionName(item.getContent(), item.getOptionType(), item.getOptionId());

                  return FinalizeOrderResponse.OrderItemInfo.builder()
                      .optionId(item.getOptionId())
                      .optionType(item.getOptionType().name())
                      .optionName(optionName)
                      .quantity(item.getQuantity())
                      .price(item.getPrice())
                      .totalPrice(item.getTotalPrice())
                      .build();
                })
            .collect(Collectors.toList());

    // 쿠폰 정보 구성
    List<FinalizeOrderResponse.AppliedCouponInfo> appliedCoupons = new ArrayList<>();
    if (appliedCoupon != null) {
      appliedCoupons.add(
          FinalizeOrderResponse.AppliedCouponInfo.builder()
              .couponCode(appliedCoupon.getCouponCode())
              .couponName(appliedCoupon.getCouponTemplate().getName())
              .discountPrice(order.getCouponDiscountPrice())
              .discountType(appliedCoupon.getCouponTemplate().getCouponType().name())
              .build());
    }

    return FinalizeOrderResponse.builder()
        .orderId(order.getId())
        .merchantUid(order.getMerchantUid())
        .originalPrice(order.getOriginalPrice())
        .couponDiscountPrice(order.getCouponDiscountPrice())
        .finalPrice(order.getFinalPrice())
        .appliedCoupons(appliedCoupons)
        .orderItems(orderItems)
        .readyForPayment(true)
        .paymentMessage("결제를 진행해주세요")
        .build();
  }

  /**
   * 옵션명 조회
   *
   * <p>ContentOption에서 옵션명을 가져옵니다. 옵션이 없는 경우 기본값을 반환합니다.
   */
  private String getOptionName(Content content, OrderItem.OptionType optionType, Long optionId) {
    if (optionType == null || optionId == null) {
      return "기본 옵션";
    }

    // Content의 옵션 리스트에서 해당 ID의 옵션을 찾습니다
    ContentOption option =
        content.getOptions().stream()
            .filter(opt -> opt.getId().equals(optionId))
            .findFirst()
            .orElse(null);

    if (option == null) {
      log.warn("옵션을 찾을 수 없습니다 - contentId: {}, optionId: {}", content.getId(), optionId);
      return "알 수 없는 옵션";
    }

    // 옵션 타입에 따른 추가 검증 (선택사항)
    if (optionType == OrderItem.OptionType.COACHING_OPTION && !(option instanceof CoachingOption)) {
      log.warn(
          "옵션 타입 불일치 - expected: COACHING_OPTION, actual: {}", option.getClass().getSimpleName());
    } else if (optionType == OrderItem.OptionType.DOCUMENT_OPTION
        && !(option instanceof DocumentOption)) {
      log.warn(
          "옵션 타입 불일치 - expected: DOCUMENT_OPTION, actual: {}", option.getClass().getSimpleName());
    }

    return option.getName();
  }
}
