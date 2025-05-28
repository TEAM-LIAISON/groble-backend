package liaison.groble.application.order;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.content.ContentReader;
import liaison.groble.application.order.dto.CreateInitialOrderDto;
import liaison.groble.application.order.dto.InitialOrderResponse;
import liaison.groble.application.order.dto.OrderCreateDto;
import liaison.groble.application.order.dto.ValidatedOrderOptionDto;
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

  /**
   * 초기 주문 생성 - 여러 옵션 지원
   *
   * <p>이 메서드는 사용자가 한 콘텐츠에서 여러 옵션을 선택하여 구매할 수 있도록 합니다. 예를 들어, 코칭 옵션 A를 2개, 코칭 옵션 B를 3개와 같이 장바구니 형태의
   * 주문을 생성합니다.
   */
  @Transactional
  public InitialOrderResponse createInitialOrder(CreateInitialOrderDto dto) {
    // 1단계: 사용자와 콘텐츠 조회
    // UserReader와 ContentReader를 사용하여 일관된 조회 로직을 유지합니다
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
            purchaser,
            dto.getOrderNote());

    // 6단계: 주문 저장 및 merchantUid 생성
    order = orderRepository.save(order);
    order.setMerchantUid(Order.generateMerchantUid(order.getId()));

    log.info(
        "초기 주문 생성 완료 - orderId: {}, userId: {}, contentId: {}, 옵션수: {}, 총액: {}",
        order.getId(),
        dto.getUserId(),
        dto.getContentId(),
        validatedOptions.size(),
        order.getFinalAmount());

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
        .originalAmount(order.getOriginalAmount())
        .discountAmount(order.getDiscountAmount())
        .finalAmount(order.getFinalAmount())
        .orderItems(orderItemResponses)
        .build();
  }

  /** 페이플 결제를 위한 주문 생성 메서드 - 쿠폰 적용 지원 - 옵션 타입 문자열로 받아 처리 */
  @Transactional
  public OrderCreateDto createOrder(
      Long userId, Long contentId, String optionTypeStr, Long optionId, String couponCode) {

    User user = userReader.getUserById(userId);
    Content content = contentReader.getContentById(contentId);

    OrderItem.OptionType optionType = null;
    if (optionTypeStr != null && !optionTypeStr.isEmpty()) {
      try {
        optionType = OrderItem.OptionType.valueOf(optionTypeStr);
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException("잘못된 옵션 타입입니다: " + optionTypeStr);
      }
    }

    // 4. 옵션에 따른 가격 조회
    BigDecimal price = getOptionPrice(content, optionType, optionId);

    // 5. 구매자 정보 생성
    Purchaser purchaser =
        Purchaser.builder()
            .name(user.getNickname())
            .email(user.getEmail())
            .phone(user.getPhoneNumber())
            .build();

    // 6. 쿠폰 조회 및 검증 (선택사항)
    UserCoupon userCoupon = null;
    if (couponCode != null) {
      userCoupon =
          userCouponRepository
              .findByCouponCode(couponCode)
              .orElseThrow(() -> new IllegalArgumentException("쿠폰을 찾을 수 없습니다."));

      // 쿠폰 사용 가능 여부 검증
      if (!userCoupon.isUsable()) {
        throw new IllegalArgumentException("사용할 수 없는 쿠폰입니다.");
      }

      // 쿠폰 소유자 검증
      if (!userCoupon.getUser().getId().equals(userId)) {
        throw new IllegalArgumentException("본인의 쿠폰만 사용할 수 있습니다.");
      }
    }

    // 7. 주문 생성 (쿠폰 적용 포함)
    Order order =
        Order.createOrderWithCoupon(
            user, content, optionType, optionId, price, userCoupon, purchaser);

    // 8. 주문 저장
    Order savedOrder = orderRepository.save(order);

    String merchantUid = Order.generateMerchantUid(order.getId());
    order.setMerchantUid(merchantUid);

    log.info(
        "주문 생성 완료 - merchantUid: {}, userId: {}, contentId: {}, finalAmount: {}",
        savedOrder.getMerchantUid(),
        userId,
        contentId,
        savedOrder.getFinalAmount());

    return OrderCreateDto.builder()
        .merchantUid(savedOrder.getMerchantUid())
        .contentId(contentId)
        .optionId(optionId)
        .price(price)
        .quantity(1) // 기본 수량 1로 설정
        .totalPrice(price)
        .build();
  }

  //  /**
  //   * 사용자별 주문 목록 조회
  //   */
  //  @Transactional(readOnly = true)
  //  public List<Order> findByUserId(Long userId, int page, int size) {
  //    PageRequest pageRequest = PageRequest.of(page, size);
  //    return orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageRequest);
  //  }

  /** 주문 취소 (결제 전 상태에서만 가능) */
  @Transactional
  public void cancelOrder(Order order, String reason) {
    if (order.getStatus() != Order.OrderStatus.PENDING) {
      throw new IllegalStateException("결제 대기 상태에서만 취소 가능합니다.");
    }

    order.cancelOrder(reason);
    orderRepository.save(order);

    log.info("주문 취소 완료 - orderId: {}, reason: {}", order.getId(), reason);
  }

  /** 주문 저장 */
  @Transactional
  public Order save(Order order) {
    return orderRepository.save(order);
  }

  /** 옵션별 가격 조회 */
  private BigDecimal getOptionPrice(
      Content content, OrderItem.OptionType optionType, Long optionId) {
    ContentOption option =
        content.getOptions().stream()
            .filter(opt -> opt.getId().equals(optionId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("옵션을 찾을 수 없습니다: " + optionId));

    return option.getPrice();
  }

  /** 옵션명 조회 */
  private String getOptionName(Content content, OrderItem.OptionType optionType, Long optionId) {
    if (optionType == null || optionId == null) {
      return "기본 옵션";
    }

    ContentOption option =
        content.getOptions().stream()
            .filter(opt -> opt.getId().equals(optionId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("옵션을 찾을 수 없습니다: " + optionId));

    return option.getName();
  }
}
