package liaison.groble.application.purchase.strategy;

import liaison.groble.application.content.ContentReviewReader;
import liaison.groble.application.content.ContentReviewWriter;
import liaison.groble.application.notification.dto.KakaoNotificationDTO;
import liaison.groble.application.notification.enums.KakaoNotificationType;
import liaison.groble.application.notification.service.KakaoNotificationService;
import liaison.groble.application.notification.service.NotificationService;
import liaison.groble.application.order.service.OrderReader;
import liaison.groble.application.purchase.dto.PurchaserContentReviewDTO;
import liaison.groble.application.purchase.service.PurchaseReader;
import liaison.groble.common.context.UserContext;
import liaison.groble.domain.content.entity.Content;
import liaison.groble.domain.content.entity.ContentReview;
import liaison.groble.domain.content.enums.ReviewStatus;
import liaison.groble.domain.order.entity.Order;
import liaison.groble.domain.purchase.entity.Purchase;

import lombok.RequiredArgsConstructor;

/**
 * 리뷰 처리 전략의 기본 클래스
 *
 * <p>Template Method Pattern을 사용하여 공통 로직을 정의하고, 사용자 타입별 차이점만 서브클래스에서 구현합니다.
 */
@RequiredArgsConstructor
public abstract class BaseReviewProcessor implements ReviewProcessorStrategy {

  protected final PurchaseReader purchaseReader;
  protected final OrderReader orderReader;
  protected final ContentReviewReader contentReviewReader;
  protected final ContentReviewWriter contentReviewWriter;
  protected final NotificationService notificationService;
  protected final KakaoNotificationService kakaoNotificationService;

  @Override
  public final PurchaserContentReviewDTO addReview(
      UserContext userContext, String merchantUid, PurchaserContentReviewDTO reviewDTO) {

    validateUserType(userContext);

    Order order = orderReader.getOrderByMerchantUid(merchantUid);
    Purchase purchase = purchaseReader.getPurchaseByOrderId(order.getId());
    Content content = purchase.getContent();

    Long userId = userContext.getId();

    validatePurchase(userId, content.getId());
    validateReviewNotExists(userId, content.getId());

    ContentReview contentReview = createContentReview(userContext, purchase, content, reviewDTO);
    ContentReview savedContentReview = contentReviewWriter.save(contentReview);

    String reviewerName = getUserDisplayName(userContext);
    sendReviewNotifications(content, purchase, reviewerName, savedContentReview.getId());

    return PurchaserContentReviewDTO.builder()
        .rating(savedContentReview.getRating())
        .reviewContent(savedContentReview.getReviewContent())
        .build();
  }

  @Override
  public final ContentReview getContentReview(UserContext userContext, Long reviewId) {
    validateUserTypeForUpdate(userContext);
    return doGetContentReview(userContext, reviewId);
  }

  @Override
  public final PurchaserContentReviewDTO updateReview(
      UserContext userContext, Long reviewId, PurchaserContentReviewDTO reviewDTO) {
    validateUserTypeForUpdate(userContext);

    ContentReview contentReview = doGetContentReview(userContext, reviewId);
    contentReview.updateReview(reviewDTO.getRating(), reviewDTO.getReviewContent());

    ContentReview savedContentReview = contentReviewWriter.save(contentReview);

    return PurchaserContentReviewDTO.builder()
        .rating(savedContentReview.getRating())
        .reviewContent(savedContentReview.getReviewContent())
        .build();
  }

  @Override
  public final void deleteReview(UserContext userContext, Long reviewId) {
    validateUserTypeForDelete(userContext);
    doDeleteReview(userContext, reviewId);
  }

  // Template Methods - 서브클래스에서 구현
  protected abstract void validateUserType(UserContext userContext);

  protected abstract void validateUserTypeForUpdate(UserContext userContext);

  protected abstract void validateUserTypeForDelete(UserContext userContext);

  protected abstract void validatePurchase(Long userId, Long contentId);

  protected abstract void validateReviewNotExists(Long userId, Long contentId);

  protected abstract ContentReview createContentReview(
      UserContext userContext,
      Purchase purchase,
      Content content,
      PurchaserContentReviewDTO reviewDTO);

  protected abstract ContentReview doGetContentReview(UserContext userContext, Long reviewId);

  protected abstract void doDeleteReview(UserContext userContext, Long reviewId);

  protected abstract String getUserDisplayName(UserContext userContext);

  // Hook Method - 공통 구현 (오버라이드 가능)
  protected final void sendReviewNotifications(
      Content content, Purchase purchase, String reviewerName, Long reviewId) {
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
            .reviewId(reviewId)
            .build());
  }

  // Helper method for ContentReview creation
  protected final ContentReview.ContentReviewBuilder getBaseContentReviewBuilder(
      Purchase purchase, Content content, PurchaserContentReviewDTO reviewDTO) {
    return ContentReview.builder()
        .content(content)
        .purchase(purchase)
        .rating(reviewDTO.getRating())
        .reviewContent(reviewDTO.getReviewContent())
        .reviewStatus(ReviewStatus.ACTIVE);
  }
}
