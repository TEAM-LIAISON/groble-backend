package liaison.groble.application.purchase.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.content.ContentReviewReader;
import liaison.groble.application.content.ContentReviewWriter;
import liaison.groble.application.guest.reader.GuestUserReader;
import liaison.groble.application.notification.dto.KakaoNotificationDTO;
import liaison.groble.application.notification.enums.KakaoNotificationType;
import liaison.groble.application.notification.service.KakaoNotificationService;
import liaison.groble.application.notification.service.NotificationService;
import liaison.groble.application.order.service.OrderReader;
import liaison.groble.application.purchase.dto.PurchaserContentReviewDTO;
import liaison.groble.application.purchase.exception.ReviewAuthenticationRequiredException;
import liaison.groble.application.purchase.strategy.ReviewProcessorFactory;
import liaison.groble.application.purchase.strategy.ReviewProcessorStrategy;
import liaison.groble.application.user.service.UserReader;
import liaison.groble.common.context.UserContext;
import liaison.groble.common.factory.UserContextFactory;
import liaison.groble.domain.content.entity.Content;
import liaison.groble.domain.content.entity.ContentReview;
import liaison.groble.domain.guest.entity.GuestUser;
import liaison.groble.domain.order.entity.Order;
import liaison.groble.domain.purchase.entity.Purchase;
import liaison.groble.domain.user.entity.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PurchaserReviewService {

  // Reader
  private final UserReader userReader;
  private final GuestUserReader guestUserReader;
  private final ContentReviewReader contentReviewReader;
  private final PurchaseReader purchaseReader;

  // Writer
  private final ContentReviewWriter contentReviewWriter;
  private final OrderReader orderReader;
  private final NotificationService notificationService;
  private final KakaoNotificationService kakaoNotificationService;

  // Strategy Factory
  private final ReviewProcessorFactory reviewProcessorFactory;

  // 통합 리뷰 추가 (회원/비회원 자동 판단) - Strategy 패턴 적용
  @Transactional
  public PurchaserContentReviewDTO addReviewUnified(
      Long userId,
      Long guestUserId,
      String merchantUid,
      PurchaserContentReviewDTO purchaserContentReviewDTO) {

    Order order = orderReader.getOrderByMerchantUid(merchantUid);
    Purchase purchase = purchaseReader.getPurchaseByOrderId(order.getId());
    Content content = purchase.getContent();

    // UserContext 생성
    UserContext userContext = createUserContext(userId, guestUserId);

    // Strategy 패턴으로 처리
    ReviewProcessorStrategy processor = reviewProcessorFactory.getProcessor(userContext);
    PurchaserContentReviewDTO result =
        processor.addReview(userContext, order, purchase, content, purchaserContentReviewDTO);

    // 알림 발송 (공통 로직)
    String reviewerName = getUserDisplayName(userContext);
    sendReviewNotifications(content, purchase, reviewerName);

    return result;
  }

  // 통합 리뷰 수정 (회원/비회원 자동 판단) - Strategy 패턴 적용
  @Transactional
  public PurchaserContentReviewDTO updateReviewUnified(
      Long userId,
      Long guestUserId,
      Long reviewId,
      PurchaserContentReviewDTO purchaserContentReviewDTO) {

    // UserContext 생성
    UserContext userContext = createUserContext(userId, guestUserId);

    // Strategy 패턴으로 리뷰 조회
    ReviewProcessorStrategy processor = reviewProcessorFactory.getProcessor(userContext);
    ContentReview contentReview = processor.getContentReview(userContext, reviewId);

    // 리뷰 업데이트
    contentReview.updateReview(
        purchaserContentReviewDTO.getRating(), purchaserContentReviewDTO.getReviewContent());

    return PurchaserContentReviewDTO.builder()
        .rating(purchaserContentReviewDTO.getRating())
        .reviewContent(purchaserContentReviewDTO.getReviewContent())
        .build();
  }

  // 통합 리뷰 삭제 (회원/비회원 자동 판단) - Strategy 패턴 적용
  @Transactional
  public void deleteReviewUnified(Long userId, Long guestUserId, Long reviewId) {
    // UserContext 생성
    UserContext userContext = createUserContext(userId, guestUserId);

    // Strategy 패턴으로 삭제 처리
    ReviewProcessorStrategy processor = reviewProcessorFactory.getProcessor(userContext);
    processor.deleteReview(userContext, reviewId);
  }

  /** UserContext 생성 유틸 메서드 */
  private UserContext createUserContext(Long userId, Long guestUserId) {
    try {
      return UserContextFactory.from(userId, guestUserId);
    } catch (IllegalArgumentException e) {
      throw ReviewAuthenticationRequiredException.forReviewAdd();
    }
  }

  /** 사용자 표시 이름 조회 유틸 메서드 */
  private String getUserDisplayName(UserContext userContext) {
    if (userContext.isMember()) {
      User user = userReader.getUserById(userContext.getId());
      return user.getNickname();
    } else {
      GuestUser guestUser = guestUserReader.getGuestUserById(userContext.getId());
      return guestUser.getUsername();
    }
  }

  /** 리뷰 알림 발송 (공통 로직) */
  private void sendReviewNotifications(Content content, Purchase purchase, String reviewerName) {
    // 기존 알림 발송 로직을 여기로 이동
    notificationService.sendContentReviewNotification(
        content.getUser(), content.getId(), purchase.getId(), content.getThumbnailUrl());

    kakaoNotificationService.sendNotification(
        KakaoNotificationDTO.builder()
            .type(KakaoNotificationType.REVIEW_REGISTERED)
            .phoneNumber(content.getUser().getPhoneNumber())
            .buyerName(reviewerName)
            .sellerName(content.getUser().getNickname())
            .contentTitle(content.getTitle())
            .contentId(content.getId())
            .reviewId(purchase.getId()) // 실제로는 ContentReview ID가 필요할 수 있습니다
            .build());
  }
}
