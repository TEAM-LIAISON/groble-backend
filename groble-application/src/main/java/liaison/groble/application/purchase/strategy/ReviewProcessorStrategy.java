package liaison.groble.application.purchase.strategy;

import liaison.groble.application.purchase.dto.PurchaserContentReviewDTO;
import liaison.groble.common.context.UserContext;
import liaison.groble.common.strategy.UserTypeProcessor;
import liaison.groble.domain.content.entity.ContentReview;

/** 리뷰 처리 전략 인터페이스 회원/비회원에 따른 리뷰 처리 로직을 추상화합니다. */
public interface ReviewProcessorStrategy extends UserTypeProcessor {

  /** 리뷰 추가 */
  PurchaserContentReviewDTO addReview(
      UserContext userContext, String merchantUid, PurchaserContentReviewDTO reviewDTO);

  /** 리뷰 조회 (사용자 타입별) */
  ContentReview getContentReview(UserContext userContext, Long reviewId);

  PurchaserContentReviewDTO updateReview(
      UserContext userContext, Long reviewId, PurchaserContentReviewDTO reviewDTO);

  /** 리뷰 삭제 (사용자 타입별) */
  void deleteReview(UserContext userContext, Long reviewId);
}
