package liaison.groble.application.purchase.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.content.ContentReviewReader;
import liaison.groble.application.content.ContentReviewWriter;
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
  private final ContentReviewReader contentReviewReader;
  private final PurchaseReader purchaseReader;

  // Writer
  private final ContentReviewWriter contentReviewWriter;
  private final OrderReader orderReader;
  private final NotificationService notificationService;
  private final KakaoNotificationService kakaoNotificationService;

  @Transactional
  public PurchaserContentReviewDTO addReview(
      Long userId, String merchantUid, PurchaserContentReviewDTO purchaserContentReviewDTO) {

    Order order = orderReader.getOrderByMerchantUid(merchantUid);
    User user = userReader.getUserById(userId);
    Purchase purchase = purchaseReader.getPurchaseByOrderId(order.getId());
    Content content = purchase.getContent();

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
            .rating(purchaserContentReviewDTO.getRating())
            .reviewContent(purchaserContentReviewDTO.getReviewContent())
            .reviewStatus(ReviewStatus.ACTIVE)
            .build();

    ContentReview savedContentReview = contentReviewWriter.save(contentReview);

    notificationService.sendContentReviewNotification(
        content.getUser(), content.getId(), savedContentReview.getId(), content.getThumbnailUrl());

    kakaoNotificationService.sendNotification(
        KakaoNotificationDTO.builder()
            .type(KakaoNotificationType.REVIEW_REGISTERED)
            .phoneNumber(content.getUser().getPhoneNumber())
            .buyerName(user.getNickname())
            .sellerName(content.getUser().getNickname())
            .contentTitle(content.getTitle())
            .contentId(content.getId())
            .reviewId(savedContentReview.getId())
            .build());

    return PurchaserContentReviewDTO.builder()
        .rating(savedContentReview.getRating())
        .reviewContent(savedContentReview.getReviewContent())
        .build();
  }

  @Transactional
  public PurchaserContentReviewDTO updateReview(
      Long userId, Long reviewId, PurchaserContentReviewDTO purchaserContentReviewDTO) {

    ContentReview contentReview = contentReviewReader.getContentReview(userId, reviewId);
    contentReview.updateReview(
        purchaserContentReviewDTO.getRating(), purchaserContentReviewDTO.getReviewContent());

    return PurchaserContentReviewDTO.builder()
        .rating(purchaserContentReviewDTO.getRating())
        .reviewContent(purchaserContentReviewDTO.getReviewContent())
        .build();
  }

  @Transactional
  public void deleteReview(Long userId, Long reviewId) {
    ContentReview contentReview = contentReviewReader.getContentReview(userId, reviewId);
    contentReviewWriter.deleteContentReview(userId, contentReview.getId());
  }
}
