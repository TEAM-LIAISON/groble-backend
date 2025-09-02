package liaison.groble.application.purchase.strategy;

import org.springframework.stereotype.Component;

import liaison.groble.application.content.ContentReviewReader;
import liaison.groble.application.content.ContentReviewWriter;
import liaison.groble.application.guest.reader.GuestUserReader;
import liaison.groble.application.notification.service.KakaoNotificationService;
import liaison.groble.application.notification.service.NotificationService;
import liaison.groble.application.order.service.OrderReader;
import liaison.groble.application.purchase.dto.PurchaserContentReviewDTO;
import liaison.groble.application.purchase.exception.ContentNotPurchasedException;
import liaison.groble.application.purchase.exception.ReviewAlreadyExistsException;
import liaison.groble.application.purchase.exception.ReviewAuthenticationRequiredException;
import liaison.groble.application.purchase.service.PurchaseReader;
import liaison.groble.common.context.UserContext;
import liaison.groble.domain.content.entity.Content;
import liaison.groble.domain.content.entity.ContentReview;
import liaison.groble.domain.guest.entity.GuestUser;
import liaison.groble.domain.purchase.entity.Purchase;

/** 비회원(게스트) 리뷰 처리 전략 */
@Component
public class GuestReviewProcessor extends BaseReviewProcessor {

  private final GuestUserReader guestUserReader;

  public GuestReviewProcessor(
      GuestUserReader guestUserReader,
      PurchaseReader purchaseReader,
      OrderReader orderReader,
      ContentReviewReader contentReviewReader,
      ContentReviewWriter contentReviewWriter,
      NotificationService notificationService,
      KakaoNotificationService kakaoNotificationService) {
    super(
        purchaseReader,
        orderReader,
        contentReviewReader,
        contentReviewWriter,
        notificationService,
        kakaoNotificationService);
    this.guestUserReader = guestUserReader;
  }

  @Override
  public String getSupportedUserType() {
    return "GUEST";
  }

  @Override
  protected void validateUserType(UserContext userContext) {
    if (!userContext.isGuest()) {
      throw ReviewAuthenticationRequiredException.forReviewAdd();
    }
  }

  @Override
  protected void validateUserTypeForUpdate(UserContext userContext) {
    if (!userContext.isGuest()) {
      throw ReviewAuthenticationRequiredException.forReviewUpdate();
    }
  }

  @Override
  protected void validateUserTypeForDelete(UserContext userContext) {
    if (!userContext.isGuest()) {
      throw ReviewAuthenticationRequiredException.forReviewDelete();
    }
  }

  @Override
  protected void validatePurchase(Long userId, Long contentId) {
    if (!purchaseReader.isContentPurchasedByGuestUser(userId, contentId)) {
      throw ContentNotPurchasedException.forGuest(contentId);
    }
  }

  @Override
  protected void validateReviewNotExists(Long userId, Long contentId) {
    if (contentReviewReader.existsContentReviewForGuest(userId, contentId)) {
      throw ReviewAlreadyExistsException.forGuest(contentId);
    }
  }

  @Override
  protected ContentReview createContentReview(
      UserContext userContext,
      Purchase purchase,
      Content content,
      PurchaserContentReviewDTO reviewDTO) {
    GuestUser guestUser = guestUserReader.getGuestUserById(userContext.getId());
    return getBaseContentReviewBuilder(purchase, content, reviewDTO).guestUser(guestUser).build();
  }

  @Override
  protected ContentReview doGetContentReview(UserContext userContext, Long reviewId) {
    return contentReviewReader.getContentReviewForGuest(userContext.getId(), reviewId);
  }

  @Override
  protected void doDeleteReview(UserContext userContext, Long reviewId) {
    contentReviewWriter.deleteGuestContentReview(userContext.getId(), reviewId);
  }

  @Override
  protected String getUserDisplayName(UserContext userContext) {
    GuestUser guestUser = guestUserReader.getGuestUserById(userContext.getId());
    return guestUser.getUsername();
  }
}
