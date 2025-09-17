package liaison.groble.application.order.strategy;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import liaison.groble.application.content.ContentReader;
import liaison.groble.application.guest.reader.GuestUserReader;
import liaison.groble.application.order.dto.CreateOrderRequestDTO;
import liaison.groble.application.order.dto.CreateOrderSuccessDTO;
import liaison.groble.application.purchase.service.PurchaseReader;
import liaison.groble.application.terms.dto.TermsAgreementDTO;
import liaison.groble.application.terms.service.OrderTermsService;
import liaison.groble.common.context.UserContext;
import liaison.groble.common.event.EventPublisher;
import liaison.groble.domain.content.entity.Content;
import liaison.groble.domain.coupon.entity.UserCoupon;
import liaison.groble.domain.coupon.repository.UserCouponRepository;
import liaison.groble.domain.guest.entity.GuestUser;
import liaison.groble.domain.guest.repository.GuestUserRepository;
import liaison.groble.domain.order.entity.Order;
import liaison.groble.domain.order.repository.OrderRepository;
import liaison.groble.domain.order.vo.OrderOptionInfo;
import liaison.groble.domain.payment.repository.PaymentRepository;
import liaison.groble.domain.purchase.repository.PurchaseRepository;

import lombok.extern.slf4j.Slf4j;

/** 비회원 주문 처리 전략 구현체 */
@Slf4j
@Component
public class GuestOrderProcessor extends BaseOrderProcessor {

  public GuestOrderProcessor(
      ContentReader contentReader,
      PurchaseReader purchaseReader,
      GuestUserReader guestUserReader,
      OrderRepository orderRepository,
      UserCouponRepository userCouponRepository,
      PurchaseRepository purchaseRepository,
      PaymentRepository paymentRepository,
      GuestUserRepository guestUserRepository,
      OrderTermsService orderTermsService,
      EventPublisher eventPublisher) {
    super(
        contentReader,
        purchaseReader,
        guestUserReader, // ← 추가!
        orderRepository,
        userCouponRepository,
        purchaseRepository,
        paymentRepository,
        guestUserRepository, // ← 추가!
        orderTermsService,
        eventPublisher);
  }

  @Override
  public String getSupportedUserType() {
    return "GUEST";
  }

  @Override
  protected Order createOrderByUserType(
      UserContext userContext, Content content, List<OrderOptionInfo> orderOptions) {
    // 게스트 사용자 조회
    GuestUser guestUser = guestUserReader.getGuestUserById(userContext.getId());

    // GuestUser 인증 상태 확인
    if (!guestUser.isVerified()) {
      throw new IllegalStateException("전화번호 인증이 완료되지 않은 게스트 사용자입니다.");
    }

    // 비회원 주문 생성 (GuestUser 연계)
    return Order.createGuestOrderWithMultipleOptions(guestUser, content, orderOptions);
  }

  @Override
  protected void validateOrderAccess(Order order, UserContext userContext) {
    if (order.getGuestUser() == null || !order.getGuestUser().getId().equals(userContext.getId())) {
      throw new IllegalStateException("해당 주문에 대한 접근 권한이 없습니다. orderId=" + order.getId());
    }
  }

  @Override
  protected CreateOrderSuccessDTO buildCreateOrderResponse(
      Order order, UserContext userContext, boolean isPurchasedContent) {
    // 게스트 사용자 정보 조회
    final GuestUser guestUser = guestUserReader.getGuestUserById(userContext.getId());

    // 첫 번째 주문 항목의 콘텐츠 제목
    String contentTitle = order.getOrderItems().get(0).getContent().getTitle();

    return CreateOrderSuccessDTO.builder()
        .merchantUid(order.getMerchantUid())
        .email(guestUser.getEmail())
        .phoneNumber(guestUser.getPhoneNumber())
        .contentTitle(contentTitle)
        .totalPrice(order.getTotalPrice())
        .isPurchasedContent(isPurchasedContent)
        .build();
  }

  @Override
  protected UserCoupon findAndValidateBestCoupon(
      Order order, UserContext userContext, List<String> couponCodes) {
    // 비회원은 쿠폰 사용 불가
    log.info("비회원 사용자는 쿠폰을 사용할 수 없습니다. guestUserId: {}", userContext.getId());
    return null;
  }

  @Override
  protected void processTermsAgreement(
      UserContext userContext, HttpServletRequest httpRequest, boolean buyerInfoStorageAgreed) {
    try {
      TermsAgreementDTO termsAgreementDTO = createTermsAgreementDTO(buyerInfoStorageAgreed);
      // IP 및 User-Agent 설정
      termsAgreementDTO.setIpAddress(httpRequest.getRemoteAddr());
      termsAgreementDTO.setUserAgent(httpRequest.getHeader("User-Agent"));

      orderTermsService.agreeToOrderTermsForGuest(termsAgreementDTO, userContext.getId());
      log.info(
          "비회원 주문 약관 동의 처리 완료 - guestUserId: {}, buyerInfoStorage: {}",
          userContext.getId(),
          buyerInfoStorageAgreed);

    } catch (Exception e) {
      log.error("비회원 주문 약관 동의 처리 실패 - guestUserId: {}", userContext.getId(), e);
      // 약관 동의 실패는 주문을 중단시키지 않음
    }
  }

  @Override
  protected void handlePostFreeOrderProcessing(
      Order order, UserContext userContext, CreateOrderRequestDTO createOrderRequestDTO) {
    // 무료 결제 성공 시점이므로 동의 승격 처리
    if (userContext.isGuest()) {
      applyBuyerConsentAfterPaymentSuccess(order);
    }
  }

  @Override
  protected void handleBuyerConsentIntent(
      Order order, UserContext userContext, CreateOrderRequestDTO createOrderRequestDTO) {
    if (!userContext.isGuest()) {
      return;
    }

    String emailSnapshot = safeTrim(order.getGuestUser().getEmail());
    String usernameSnapshot = safeTrim(order.getGuestUser().getUsername());

    recordBuyerConsentIntentAndSnapshot(
        order, createOrderRequestDTO.isBuyerInfoStorageAgreed(), emailSnapshot, usernameSnapshot);
  }

  /** 주문 생성 시점: '동의 의사'와 '스냅샷(이메일/이름)'만 orders 테이블에 기록 */
  private void recordBuyerConsentIntentAndSnapshot(
      Order order, boolean consentIntent, String emailSnapshot, String usernameSnapshot) {
    order.setBuyerConsentIntentAndSnapshot(consentIntent, emailSnapshot, usernameSnapshot);
    orderRepository.save(order); // 변경 필드 반영
  }

  /**
   * 결제 성공 시점에만 호출: - intent=true 이고 게스트 주문이면 GuestUser를 동의=true로 '승격' - 이메일/이름이 비어있으면 주문 스냅샷으로 보강 -
   * 부분 유니크(uk_guest_phone_when_agreed) 충돌 시 정본으로 주문 재지정
   */
  private void applyBuyerConsentAfterPaymentSuccess(Order order) {
    if (!order.isBuyerInfoConsentIntent()) return; // 의사 없으면 처리 안함
    if (!order.isGuestOrder()) return; // 게스트 주문만 해당

    GuestUser guest = guestUserReader.getGuestUserById(order.getGuestUser().getId()); // 행 잠금 권장
    if (guest.isBuyerInfoStorageAgreed()) return; // 이미 동의된 경우 스킵

    try {
      guest.agreeBuyerInfo(order.getBuyerUsernameSnapshot(), order.getBuyerEmailSnapshot());
      guestUserRepository.save(guest); // 여기서 uk_guest_phone_when_agreed 제약이 작동
    } catch (DataIntegrityViolationException e) {
      // 같은 전화번호로 이미 동의=true인 정본이 존재 → 그 정본으로 주문 연결 변경
      GuestUser canonical =
          guestUserReader.getByPhoneNumberAndBuyerInfoStorageAgreedTrue(guest.getPhoneNumber());
      order.setGuestUserId(canonical);
      orderRepository.save(order);
      // 필요 시 mergeGuestData(guest, canonical); // 선택: 비정본 데이터 보강/아카이브
    }
  }

  private String safeTrim(String v) {
    return (v == null) ? null : v.trim();
  }
}
