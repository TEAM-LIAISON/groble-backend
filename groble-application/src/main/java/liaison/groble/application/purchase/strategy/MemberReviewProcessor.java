package liaison.groble.application.purchase.strategy;

import org.springframework.stereotype.Component;

import liaison.groble.application.content.ContentReviewReader;
import liaison.groble.application.content.ContentReviewWriter;
import liaison.groble.application.purchase.dto.PurchaserContentReviewDTO;
import liaison.groble.application.purchase.exception.ContentNotPurchasedException;
import liaison.groble.application.purchase.exception.ReviewAlreadyExistsException;
import liaison.groble.application.purchase.exception.ReviewAuthenticationRequiredException;
import liaison.groble.application.purchase.service.PurchaseReader;
import liaison.groble.application.user.service.UserReader;
import liaison.groble.common.context.UserContext;
import liaison.groble.domain.content.entity.Content;
import liaison.groble.domain.content.entity.ContentReview;
import liaison.groble.domain.content.enums.ReviewStatus;
import liaison.groble.domain.order.entity.Order;
import liaison.groble.domain.purchase.entity.Purchase;
import liaison.groble.domain.user.entity.User;

import lombok.RequiredArgsConstructor;

/** 회원 리뷰 처리 전략 */
@Component
@RequiredArgsConstructor
public class MemberReviewProcessor implements ReviewProcessorStrategy {

  private final UserReader userReader;
  private final PurchaseReader purchaseReader;
  private final ContentReviewReader contentReviewReader;
  private final ContentReviewWriter contentReviewWriter;

  @Override
  public String getSupportedUserType() {
    return "MEMBER";
  }

  @Override
  public PurchaserContentReviewDTO addReview(
      UserContext userContext,
      Order order,
      Purchase purchase,
      Content content,
      PurchaserContentReviewDTO reviewDTO) {

    if (!userContext.isMember()) {
      throw ReviewAuthenticationRequiredException.forReviewAdd();
    }

    Long userId = userContext.getId();
    User user = userReader.getUserById(userId);

    if (!purchaseReader.isContentPurchasedByUser(userId, content.getId())) {
      throw ContentNotPurchasedException.forMember(content.getId());
    }

    if (contentReviewReader.existsContentReview(userId, content.getId())) {
      throw ReviewAlreadyExistsException.forMember(content.getId());
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

    return PurchaserContentReviewDTO.builder()
        .rating(savedContentReview.getRating())
        .reviewContent(savedContentReview.getReviewContent())
        .build();
  }

  @Override
  public ContentReview getContentReview(UserContext userContext, Long reviewId) {
    if (!userContext.isMember()) {
      throw ReviewAuthenticationRequiredException.forReviewUpdate();
    }

    return contentReviewReader.getContentReview(userContext.getId(), reviewId);
  }

  @Override
  public void deleteReview(UserContext userContext, Long reviewId) {
    if (!userContext.isMember()) {
      throw ReviewAuthenticationRequiredException.forReviewDelete();
    }

    contentReviewWriter.deleteContentReview(userContext.getId(), reviewId);
  }
}
