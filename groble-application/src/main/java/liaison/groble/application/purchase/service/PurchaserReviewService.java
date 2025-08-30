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
import liaison.groble.application.user.service.UserReader;
import liaison.groble.domain.content.entity.Content;
import liaison.groble.domain.content.entity.ContentReview;
import liaison.groble.domain.content.enums.ReviewStatus;
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

  // 통합 리뷰 추가 (회원/비회원 자동 판단)
  @Transactional
  public PurchaserContentReviewDTO addReviewUnified(
      Long userId,
      Long guestUserId,
      String merchantUid,
      PurchaserContentReviewDTO purchaserContentReviewDTO) {

    Order order = orderReader.getOrderByMerchantUid(merchantUid);
    Purchase purchase = purchaseReader.getPurchaseByOrderId(order.getId());
    Content content = purchase.getContent();

    if (userId != null) {
      // 회원 리뷰 추가
      return addReviewForMember(userId, order, purchase, content, purchaserContentReviewDTO);
    } else if (guestUserId != null) {
      // 비회원 리뷰 추가
      return addReviewForGuest(guestUserId, order, purchase, content, purchaserContentReviewDTO);
    } else {
      throw new IllegalArgumentException("userId 또는 guestUserId 중 하나는 반드시 제공되어야 합니다.");
    }
  }

  private PurchaserContentReviewDTO addReviewForMember(
      Long userId,
      Order order,
      Purchase purchase,
      Content content,
      PurchaserContentReviewDTO reviewDTO) {

    User user = userReader.getUserById(userId);

    if (!purchaseReader.isContentPurchasedByUser(userId, content.getId())) {
      throw new IllegalArgumentException("사용자가 해당 콘텐츠를 구매하지 않았습니다.");
    }

    if (contentReviewReader.existsContentReview(userId, content.getId())) {
      throw new IllegalArgumentException("이미 해당 콘텐츠에 대한 리뷰가 존재합니다.");
    }

    ContentReview contentReview =
        ContentReview.builder()
            .user(user)
            .content(content)
            .purchase(purchase)
            .rating(reviewDTO.getRating())
            .reviewContent(reviewDTO.getReviewContent())
            .reviewStatus(ReviewStatus.ACTIVE)
            .build();

    ContentReview savedContentReview = contentReviewWriter.save(contentReview);

    // 알림 발송
    sendReviewNotifications(content, savedContentReview, user.getNickname());

    return PurchaserContentReviewDTO.builder()
        .rating(savedContentReview.getRating())
        .reviewContent(savedContentReview.getReviewContent())
        .build();
  }

  private PurchaserContentReviewDTO addReviewForGuest(
      Long guestUserId,
      Order order,
      Purchase purchase,
      Content content,
      PurchaserContentReviewDTO reviewDTO) {

    GuestUser guestUser = guestUserReader.getGuestUserById(guestUserId);

    // 게스트용 구매 확인 및 중복 리뷰 체크 로직 필요 (추후 구현)

    ContentReview contentReview =
        ContentReview.builder()
            .guestUser(guestUser)
            .content(content)
            .purchase(purchase)
            .rating(reviewDTO.getRating())
            .reviewContent(reviewDTO.getReviewContent())
            .reviewStatus(ReviewStatus.ACTIVE)
            .build();

    ContentReview savedContentReview = contentReviewWriter.save(contentReview);

    // 알림 발송
    sendReviewNotifications(content, savedContentReview, guestUser.getUsername());

    return PurchaserContentReviewDTO.builder()
        .rating(savedContentReview.getRating())
        .reviewContent(savedContentReview.getReviewContent())
        .build();
  }

  private void sendReviewNotifications(
      Content content, ContentReview savedContentReview, String reviewerName) {
    notificationService.sendContentReviewNotification(
        content.getUser(), content.getId(), savedContentReview.getId(), content.getThumbnailUrl());

    kakaoNotificationService.sendNotification(
        KakaoNotificationDTO.builder()
            .type(KakaoNotificationType.REVIEW_REGISTERED)
            .phoneNumber(content.getUser().getPhoneNumber())
            .buyerName(reviewerName)
            .sellerName(content.getUser().getNickname())
            .contentTitle(content.getTitle())
            .contentId(content.getId())
            .reviewId(savedContentReview.getId())
            .build());
  }

  // 통합 리뷰 수정 (회원/비회원 자동 판단)
  @Transactional
  public PurchaserContentReviewDTO updateReviewUnified(
      Long userId,
      Long guestUserId,
      Long reviewId,
      PurchaserContentReviewDTO purchaserContentReviewDTO) {

    ContentReview contentReview = getContentReviewByUserType(userId, guestUserId, reviewId);
    contentReview.updateReview(
        purchaserContentReviewDTO.getRating(), purchaserContentReviewDTO.getReviewContent());

    return PurchaserContentReviewDTO.builder()
        .rating(purchaserContentReviewDTO.getRating())
        .reviewContent(purchaserContentReviewDTO.getReviewContent())
        .build();
  }

  @Transactional
  public PurchaserContentReviewDTO updateReview(
      Long userId, Long reviewId, PurchaserContentReviewDTO purchaserContentReviewDTO) {
    return updateReviewUnified(userId, null, reviewId, purchaserContentReviewDTO);
  }

  // 통합 리뷰 삭제 (회원/비회원 자동 판단)
  @Transactional
  public void deleteReviewUnified(Long userId, Long guestUserId, Long reviewId) {
    ContentReview contentReview = getContentReviewByUserType(userId, guestUserId, reviewId);

    if (userId != null) {
      contentReviewWriter.deleteContentReview(userId, contentReview.getId());
    } else if (guestUserId != null) {
      // 게스트용 삭제 로직 (추후 구현 필요)
      contentReviewWriter.deleteContentReview(null, contentReview.getId());
    }
  }

  @Transactional
  public void deleteReview(Long userId, Long reviewId) {
    deleteReviewUnified(userId, null, reviewId);
  }

  private ContentReview getContentReviewByUserType(Long userId, Long guestUserId, Long reviewId) {
    if (userId != null) {
      return contentReviewReader.getContentReview(userId, reviewId);
    } else if (guestUserId != null) {
      // 게스트용 리뷰 조회 로직 (추후 구현 필요)
      return contentReviewReader.getContentReviewById(reviewId);
    } else {
      throw new IllegalArgumentException("userId 또는 guestUserId 중 하나는 반드시 제공되어야 합니다.");
    }
  }
}
