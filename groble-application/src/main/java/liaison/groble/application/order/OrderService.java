package liaison.groble.application.order;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.content.ContentReader;
import liaison.groble.application.order.dto.OrderCreateDto;
import liaison.groble.application.user.service.UserReader;
import liaison.groble.domain.content.entity.Content;
import liaison.groble.domain.content.entity.ContentOption;
import liaison.groble.domain.coupon.entity.UserCoupon;
import liaison.groble.domain.coupon.repository.UserCouponRepository;
import liaison.groble.domain.order.entity.Order;
import liaison.groble.domain.order.entity.OrderItem;
import liaison.groble.domain.order.repository.OrderRepository;
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
    String optionName = getOptionName(content, optionType, optionId);

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
            user, content, optionType, optionId, optionName, price, userCoupon, purchaser);

    // 8. 주문 저장
    Order savedOrder = orderRepository.save(order);

    log.info(
        "주문 생성 완료 - orderId: {}, userId: {}, contentId: {}, finalAmount: {}",
        savedOrder.getId(),
        userId,
        contentId,
        savedOrder.getFinalAmount());

    return OrderCreateDto.builder()
        .orderId(savedOrder.getId())
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
