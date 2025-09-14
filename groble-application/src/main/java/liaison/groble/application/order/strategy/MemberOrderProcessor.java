package liaison.groble.application.order.strategy;

import java.math.BigDecimal;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;

import liaison.groble.application.content.ContentReader;
import liaison.groble.application.order.dto.CreateOrderSuccessDTO;
import liaison.groble.application.purchase.service.PurchaseReader;
import liaison.groble.application.terms.dto.TermsAgreementDTO;
import liaison.groble.application.terms.service.OrderTermsService;
import liaison.groble.application.user.service.UserReader;
import liaison.groble.common.context.UserContext;
import liaison.groble.common.event.EventPublisher;
import liaison.groble.domain.content.entity.Content;
import liaison.groble.domain.coupon.entity.UserCoupon;
import liaison.groble.domain.coupon.repository.UserCouponRepository;
import liaison.groble.domain.order.entity.Order;
import liaison.groble.domain.order.entity.Purchaser;
import liaison.groble.domain.order.repository.OrderRepository;
import liaison.groble.domain.order.vo.OrderOptionInfo;
import liaison.groble.domain.payment.repository.PaymentRepository;
import liaison.groble.domain.purchase.repository.PurchaseRepository;
import liaison.groble.domain.user.entity.User;

import lombok.extern.slf4j.Slf4j;

/** 회원 주문 처리 전략 구현체 */
@Slf4j
@Component
public class MemberOrderProcessor extends BaseOrderProcessor {

  private final UserReader userReader;

  public MemberOrderProcessor(
      ContentReader contentReader,
      PurchaseReader purchaseReader,
      OrderRepository orderRepository,
      UserCouponRepository userCouponRepository,
      PurchaseRepository purchaseRepository,
      PaymentRepository paymentRepository,
      OrderTermsService orderTermsService,
      EventPublisher eventPublisher,
      UserReader userReader) {
    super(
        contentReader,
        purchaseReader,
        orderRepository,
        userCouponRepository,
        purchaseRepository,
        paymentRepository,
        orderTermsService,
        eventPublisher);
    this.userReader = userReader;
  }

  @Override
  public String getSupportedUserType() {
    return "MEMBER";
  }

  @Override
  protected Order createOrderByUserType(
      UserContext userContext, Content content, List<OrderOptionInfo> orderOptions) {
    // 회원 조회
    final User user = userReader.getUserById(userContext.getId());

    // 회원 주문 생성
    final Purchaser purchaser = buildPurchaserFromUser(user);
    return Order.createOrderWithMultipleOptions(user, content, orderOptions, purchaser);
  }

  @Override
  protected void validateOrderAccess(Order order, UserContext userContext) {
    if (order.getUser() == null || !order.getUser().getId().equals(userContext.getId())) {
      throw new IllegalStateException("해당 주문에 대한 접근 권한이 없습니다. orderId=" + order.getId());
    }
  }

  @Override
  protected CreateOrderSuccessDTO buildCreateOrderResponse(
      Order order, UserContext userContext, boolean isPurchasedContent) {
    // 회원 정보 조회
    final User user = userReader.getUserById(userContext.getId());

    // 첫 번째 주문 항목의 콘텐츠 제목
    String contentTitle = order.getOrderItems().get(0).getContent().getTitle();

    return CreateOrderSuccessDTO.builder()
        .merchantUid(order.getMerchantUid())
        .email(user.getEmail())
        .phoneNumber(user.getPhoneNumber())
        .contentTitle(contentTitle)
        .totalPrice(order.getTotalPrice())
        .isPurchasedContent(isPurchasedContent)
        .build();
  }

  @Override
  protected UserCoupon findAndValidateBestCoupon(
      Order order, UserContext userContext, List<String> couponCodes) {
    // 무료 상품은 쿠폰 적용 불가
    if (order.getOriginalPrice().compareTo(BigDecimal.ZERO) <= 0) {
      log.info("무료 상품(원가 0원)은 쿠폰 적용이 불가능합니다");
      return null;
    }

    final User user = userReader.getUserById(userContext.getId());
    return findBestCoupon(user, couponCodes, order.getOriginalPrice());
  }

  @Override
  protected void processTermsAgreement(
      UserContext userContext, HttpServletRequest httpRequest, boolean buyerInfoStorageAgreed) {
    try {
      TermsAgreementDTO termsAgreementDTO = createTermsAgreementDTO(buyerInfoStorageAgreed);
      termsAgreementDTO.setUserId(userContext.getId());
      // IP 및 User-Agent 설정
      termsAgreementDTO.setIpAddress(httpRequest.getRemoteAddr());
      termsAgreementDTO.setUserAgent(httpRequest.getHeader("User-Agent"));

      orderTermsService.agreeToOrderTerms(termsAgreementDTO);
      log.info(
          "회원 주문 약관 동의 처리 완료 - userId: {}, buyerInfoStorage: {}",
          userContext.getId(),
          buyerInfoStorageAgreed);

    } catch (Exception e) {
      log.error("회원 주문 약관 동의 처리 실패 - userId: {}", userContext.getId(), e);
      // 약관 동의 실패는 주문을 중단시키지 않음
    }
  }

  /** 사용자 정보로부터 구매자 정보 생성 */
  private Purchaser buildPurchaserFromUser(User user) {
    return Purchaser.builder()
        .name(user.getNickname())
        .email(user.getEmail())
        .phone(user.getPhoneNumber())
        .build();
  }

  /** 가장 유리한 쿠폰 찾기 */
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

  /** 쿠폰 유효성 검증 */
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

  /** 쿠폰 할인 금액 계산 */
  private BigDecimal calculateCouponDiscount(UserCoupon coupon, BigDecimal orderPrice) {
    return coupon.getCouponTemplate().calculateDiscountPrice(orderPrice);
  }
}
