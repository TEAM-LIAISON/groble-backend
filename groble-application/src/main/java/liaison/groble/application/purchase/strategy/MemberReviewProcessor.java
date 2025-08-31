package liaison.groble.application.purchase.strategy;

import org.springframework.stereotype.Component;

import liaison.groble.application.content.ContentReviewReader;
import liaison.groble.application.content.ContentReviewWriter;
import liaison.groble.application.notification.service.KakaoNotificationService;
import liaison.groble.application.notification.service.NotificationService;
import liaison.groble.application.order.service.OrderReader;
import liaison.groble.application.purchase.dto.PurchaserContentReviewDTO;
import liaison.groble.application.purchase.exception.ContentNotPurchasedException;
import liaison.groble.application.purchase.exception.ReviewAlreadyExistsException;
import liaison.groble.application.purchase.exception.ReviewAuthenticationRequiredException;
import liaison.groble.application.purchase.service.PurchaseReader;
import liaison.groble.application.user.service.UserReader;
import liaison.groble.common.context.UserContext;
import liaison.groble.domain.content.entity.Content;
import liaison.groble.domain.content.entity.ContentReview;
import liaison.groble.domain.purchase.entity.Purchase;
import liaison.groble.domain.user.entity.User;

/** 회원 리뷰 처리 전략 */
@Component
public class MemberReviewProcessor extends BaseReviewProcessor {

  private final UserReader userReader;

  public MemberReviewProcessor(
      UserReader userReader,
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
    this.userReader = userReader;
  }

  @Override
  public String getSupportedUserType() {
    return "MEMBER";
  }

  @Override
  protected void validateUserType(UserContext userContext) {
    if (!userContext.isMember()) {
      throw ReviewAuthenticationRequiredException.forReviewAdd();
    }
  }

  @Override
  protected void validateUserTypeForUpdate(UserContext userContext) {
    if (!userContext.isMember()) {
      throw ReviewAuthenticationRequiredException.forReviewUpdate();
    }
  }

  @Override
  protected void validateUserTypeForDelete(UserContext userContext) {
    if (!userContext.isMember()) {
      throw ReviewAuthenticationRequiredException.forReviewDelete();
    }
  }

  @Override
  protected void validatePurchase(Long userId, Long contentId) {
    if (!purchaseReader.isContentPurchasedByUser(userId, contentId)) {
      throw ContentNotPurchasedException.forMember(contentId);
    }
  }

  @Override
  protected void validateReviewNotExists(Long userId, Long contentId) {
    if (contentReviewReader.existsContentReview(userId, contentId)) {
      throw ReviewAlreadyExistsException.forMember(contentId);
    }
  }

  @Override
  protected ContentReview createContentReview(
      UserContext userContext,
      Purchase purchase,
      Content content,
      PurchaserContentReviewDTO reviewDTO) {
    User user = userReader.getUserById(userContext.getId());
    return getBaseContentReviewBuilder(purchase, content, reviewDTO).user(user).build();
  }

  @Override
  protected ContentReview doGetContentReview(UserContext userContext, Long reviewId) {
    return contentReviewReader.getContentReview(userContext.getId(), reviewId);
  }

  @Override
  protected void doDeleteReview(UserContext userContext, Long reviewId) {
    contentReviewWriter.deleteContentReview(userContext.getId(), reviewId);
  }

  @Override
  protected String getUserDisplayName(UserContext userContext) {
    User user = userReader.getUserById(userContext.getId());
    return user.getNickname();
  }
}
