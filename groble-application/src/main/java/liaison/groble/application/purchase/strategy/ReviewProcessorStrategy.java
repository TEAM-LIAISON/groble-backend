package liaison.groble.application.purchase.strategy;

import liaison.groble.application.purchase.dto.PurchaserContentReviewDTO;
import liaison.groble.common.context.UserContext;
import liaison.groble.domain.content.entity.Content;
import liaison.groble.domain.content.entity.ContentReview;
import liaison.groble.domain.order.entity.Order;
import liaison.groble.domain.purchase.entity.Purchase;

/** 리뷰 처리 전략 인터페이스 회원/비회원에 따른 리뷰 처리 로직을 추상화합니다. */
public interface ReviewProcessorStrategy {

  /**
   * 전략이 지원하는 사용자 타입
   *
   * @return "MEMBER" 또는 "GUEST"
   */
  String getSupportedUserType();

  /** 리뷰 추가 */
  PurchaserContentReviewDTO addReview(
      UserContext userContext,
      Order order,
      Purchase purchase,
      Content content,
      PurchaserContentReviewDTO reviewDTO);

  /** 리뷰 조회 (사용자 타입별) */
  ContentReview getContentReview(UserContext userContext, Long reviewId);

  /** 리뷰 삭제 (사용자 타입별) */
  void deleteReview(UserContext userContext, Long reviewId);
}
