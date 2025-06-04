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
import liaison.groble.application.order.dto.ValidatedOrderOptionDto;
import liaison.groble.application.user.service.UserReader;
import liaison.groble.domain.content.entity.Content;
import liaison.groble.domain.content.entity.ContentOption;
import liaison.groble.domain.coupon.entity.UserCoupon;
import liaison.groble.domain.coupon.repository.UserCouponRepository;
import liaison.groble.domain.order.entity.Order;
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
   * 사용자 주문 생성
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
    }

    // 8단계: 최종 주문 저장
    order = orderRepository.save(order);

    log.info(
        "주문 생성 완료 - orderId: {}, userId: {}, contentId: {}, 옵션수: {}, 최종금액: {}",
        order.getId(),
        userId,
        createOrderDto.getContentId(),
        validatedOptions.size(),
        order.getFinalPrice());

    return buildCreateOrderResponse(order);
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

    // 9단계: 최종 주문 저장
    order = orderRepository.save(order);

    log.info(
        "비회원 주문 생성 완료 - orderId: {}, email: {}, contentId: {}, 옵션수: {}, 최종금액: {}",
        order.getId(),
        createOrderDto.getEmail(),
        createOrderDto.getContentId(),
        validatedOptions.size(),
        order.getFinalPrice());

    return buildPublicOrderResponse(order);
  }

  /**
   * CreateOrderResponse 생성 (회원 주문용)
   *
   * <p>생성된 주문 정보를 바탕으로 클라이언트에 반환할 응답을 구성합니다.
   */
  private CreateOrderResponse buildCreateOrderResponse(Order order) {
    return CreateOrderResponse.builder()
        .merchantUid(order.getMerchantUid())
        .email(order.getUser().getEmail())
        .phoneNumber(order.getUser().getPhoneNumber())
        .contentTitle(order.getOrderItems().get(0).getContent().getTitle())
        .totalPrice(order.getTotalPrice())
        .build();
  }

  /**
   * CreateOrderResponse 생성 (비회원 주문용)
   *
   * <p>비회원 주문의 경우 Purchaser 정보를 사용하여 응답을 구성합니다.
   */
  private CreateOrderResponse buildPublicOrderResponse(Order order) {
    return CreateOrderResponse.builder()
        .merchantUid(order.getMerchantUid())
        .email(order.getPurchaser().getEmail())
        .phoneNumber(order.getPurchaser().getPhone())
        .contentTitle(order.getOrderItems().get(0).getContent().getTitle())
        .totalPrice(order.getTotalPrice())
        .build();
  }
}
