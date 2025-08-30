package liaison.groble.application.purchase.strategy;

import org.springframework.stereotype.Component;

import liaison.groble.application.content.ContentReviewReader;
import liaison.groble.application.content.ContentReviewWriter;
import liaison.groble.application.guest.reader.GuestUserReader;
import liaison.groble.application.purchase.dto.PurchaserContentReviewDTO;
import liaison.groble.application.purchase.exception.ContentNotPurchasedException;
import liaison.groble.application.purchase.exception.ReviewAlreadyExistsException;
import liaison.groble.application.purchase.exception.ReviewAuthenticationRequiredException;
import liaison.groble.application.purchase.service.PurchaseReader;
import liaison.groble.common.context.UserContext;
import liaison.groble.domain.content.entity.Content;
import liaison.groble.domain.content.entity.ContentReview;
import liaison.groble.domain.content.enums.ReviewStatus;
import liaison.groble.domain.guest.entity.GuestUser;
import liaison.groble.domain.order.entity.Order;
import liaison.groble.domain.purchase.entity.Purchase;

import lombok.RequiredArgsConstructor;

/** 비회원(게스트) 리뷰 처리 전략 */
@Component
@RequiredArgsConstructor
public class GuestReviewProcessor implements ReviewProcessorStrategy {

  private final GuestUserReader guestUserReader;
  private final PurchaseReader purchaseReader;
  private final ContentReviewReader contentReviewReader;
  private final ContentReviewWriter contentReviewWriter;

  @Override
  public String getSupportedUserType() {
    return "GUEST";
  }

  @Override
  public PurchaserContentReviewDTO addReview(
      UserContext userContext,
      Order order,
      Purchase purchase,
      Content content,
      PurchaserContentReviewDTO reviewDTO) {

    if (!userContext.isGuest()) {
      throw ReviewAuthenticationRequiredException.forReviewAdd();
    }

    Long guestUserId = userContext.getId();
    GuestUser guestUser = guestUserReader.getGuestUserById(guestUserId);

    if (!purchaseReader.isContentPurchasedByGuestUser(guestUserId, content.getId())) {
      throw ContentNotPurchasedException.forGuest(content.getId());
    }

    if (contentReviewReader.existsContentReviewForGuest(guestUserId, content.getId())) {
      throw ReviewAlreadyExistsException.forGuest(content.getId());
    }

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

    return PurchaserContentReviewDTO.builder()
        .rating(savedContentReview.getRating())
        .reviewContent(savedContentReview.getReviewContent())
        .build();
  }

  @Override
  public ContentReview getContentReview(UserContext userContext, Long reviewId) {
    if (!userContext.isGuest()) {
      throw ReviewAuthenticationRequiredException.forReviewUpdate();
    }

    return contentReviewReader.getContentReviewForGuest(userContext.getId(), reviewId);
  }

  @Override
  public void deleteReview(UserContext userContext, Long reviewId) {
    if (!userContext.isGuest()) {
      throw ReviewAuthenticationRequiredException.forReviewDelete();
    }

    contentReviewWriter.deleteGuestContentReview(userContext.getId(), reviewId);
  }
}
